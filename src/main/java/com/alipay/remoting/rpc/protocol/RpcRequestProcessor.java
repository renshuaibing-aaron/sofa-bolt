package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.*;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.RpcCommandType;
import com.alipay.remoting.util.RemotingUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Process Rpc request.
 *
 * @author jiangping
 * @version $Id: RpcRequestProcessor.java, v 0.1 2015-10-1 PM10:56:10 tao Exp $
 */
public class RpcRequestProcessor extends AbstractRemotingProcessor<RpcRequestCommand> {
    /**
     * logger
     */
    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    /**
     * Default constructor.
     */
    public RpcRequestProcessor() {
    }

    /**
     * Constructor.
     */
    public RpcRequestProcessor(CommandFactory commandFactory) {
        super(commandFactory);
    }

    /**
     * Constructor.
     */
    public RpcRequestProcessor(ExecutorService executor) {
        super(executor);
    }

    /**
     * @see com.alipay.remoting.AbstractRemotingProcessor#process(com.alipay.remoting.RemotingContext, com.alipay.remoting.RemotingCommand, java.util.concurrent.ExecutorService)
     */
    @Override
    public void process(RemotingContext ctx, RpcRequestCommand cmd, ExecutorService defaultExecutor)
            throws Exception {
        System.out.println("从 CommandHandler 中获取 CommandCode 为 REQUEST 的 RemotingProcessor 实例 RpcRequestProcessor，之后使用 RpcRequestProcessor 进行请求处理");

        // 首先反序列化 clazzName，因为需要 clazzName 来获取 UserProcessor，如果处理 clazzName 的 UserProcessor 不存在，则直接返回错误
        if (!deserializeRequestCommand(ctx, cmd, RpcDeserializeLevel.DESERIALIZE_CLAZZ)) {
            return;
        }

        // 根据clazz获取UserProcessor
        UserProcessor userProcessor = ctx.getUserProcessor(cmd.getRequestClass());

        if (userProcessor == null) {
            String errMsg = "No user processor found for request: " + cmd.getRequestClass();
            logger.error(errMsg);
            sendResponseIfNecessary(ctx, cmd.getType(), this.getCommandFactory()
                    .createExceptionResponse(cmd.getId(), errMsg));
            return;// must end process
        }

        // set timeout check state from user's processor
        ctx.setTimeoutDiscard(userProcessor.timeoutDiscard());

        // 如果指定在IO线程处理请求，则直接反序列化全部，创建ProcessTask，直接执行
        // to check whether to process in io thread
        if (userProcessor.processInIOThread()) {
            if (!deserializeRequestCommand(ctx, cmd, RpcDeserializeLevel.DESERIALIZE_ALL)) {
                return;
            }
            // process in io thread
            new ProcessTask(ctx, cmd).run();
            return;// end
        }

        Executor executor;
        // to check whether get executor using executor selector
        // 如果指定不是在IO线程处理请求，则先获取线程池，创建ProcessTask，在新的线程池执行
        // 线程池的选择：userProcessor.executorSelector -> userProcessor.executor -> RemotingProcessor.executor -> ProcessorManager.defaultExecutor
        // 看是否配置了 UserProcessor.executorSelector，即线程池选择器，
        // 如果配置了：则需要反序列化出 header，因为 executorSelector 需要根据 header 去选择 executor；content 在异步线程池进行反序列化
        // 如果没有配置：则 header 和 content 都在选出的异步线程池进行反序列化
        if (null == userProcessor.getExecutorSelector()) {
            executor = userProcessor.getExecutor();
        } else {
            // in case haven't deserialized in io thread
            // it need to deserialize clazz and header before using executor dispath strategy
            if (!deserializeRequestCommand(ctx, cmd, RpcDeserializeLevel.DESERIALIZE_HEADER)) {
                return;
            }
            //try get executor with strategy
            executor = userProcessor.getExecutorSelector().select(cmd.getRequestClass(),
                    cmd.getRequestHeader());
        }

        // Till now, if executor still null, then try default
        if (executor == null) {
            executor = (this.getExecutor() == null ? defaultExecutor : this.getExecutor());
        }

        // use the final executor dispatch process task
        executor.execute(new ProcessTask(ctx, cmd));
    }

    /**
     * @see com.alipay.remoting.AbstractRemotingProcessor#doProcess(com.alipay.remoting.RemotingContext, com.alipay.remoting.RemotingCommand)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void doProcess(final RemotingContext ctx, RpcRequestCommand cmd) throws Exception {
        long currentTimestamp = System.currentTimeMillis();

        preProcessRemotingContext(ctx, cmd, currentTimestamp);
        if (ctx.isTimeoutDiscard() && ctx.isRequestTimeout()) {
            timeoutLog(cmd, currentTimestamp, ctx);// do some log
            return;// then, discard this request
        }
        debugLog(ctx, cmd, currentTimestamp);
        // decode request all
        // 反序列化全部
        if (!deserializeRequestCommand(ctx, cmd, RpcDeserializeLevel.DESERIALIZE_ALL)) {
            return;
        }
        //反序列化header、content
        dispatchToUserProcessor(ctx, cmd);
    }

    /**
     * Send response using remoting context if necessary.<br>
     * If request type is oneway, no need to send any response nor exception.
     *
     * @param ctx      remoting context
     * @param type     type code
     * @param response remoting command
     */
    public void sendResponseIfNecessary(final RemotingContext ctx, byte type,
                                        final RemotingCommand response) {
        final int id = response.getId();
        if (type != RpcCommandType.REQUEST_ONEWAY) {
            RemotingCommand serializedResponse = response;
            try {
                // 响应序列化
                response.serialize();
            } catch (SerializationException e) {
                String errMsg = "SerializationException occurred when sendResponseIfNecessary in RpcRequestProcessor, id="
                        + id;
                logger.error(errMsg, e);
                serializedResponse = this.getCommandFactory().createExceptionResponse(id,
                        ResponseStatus.SERVER_SERIAL_EXCEPTION, e);
                try {
                    serializedResponse.serialize();// serialize again for exception response
                } catch (SerializationException e1) {
                    // should not happen
                    logger.error("serialize SerializationException response failed!");
                }
            } catch (Throwable t) {
                String errMsg = "Serialize RpcResponseCommand failed when sendResponseIfNecessary in RpcRequestProcessor, id="
                        + id;
                logger.error(errMsg, t);
                serializedResponse = this.getCommandFactory()
                        .createExceptionResponse(id, t, errMsg);
            }
            // Netty 发送响应
            ctx.writeAndFlush(serializedResponse).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Rpc response sent! requestId="
                                + id
                                + ". The address is "
                                + RemotingUtil.parseRemoteAddress(ctx.getChannelContext()
                                .channel()));
                    }
                    if (!future.isSuccess()) {
                        logger.error(
                                "Rpc response send failed! id="
                                        + id
                                        + ". The address is "
                                        + RemotingUtil.parseRemoteAddress(ctx.getChannelContext()
                                        .channel()), future.cause());
                    }
                }
            });
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Oneway rpc request received, do not send response, id=" + id
                        + ", the address is "
                        + RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            }
        }
    }

    /**
     * dispatch request command to user processor
     *
     * @param ctx remoting context
     * @param cmd rpc request command
     */
    private void dispatchToUserProcessor(RemotingContext ctx, RpcRequestCommand cmd) {
        final int id = cmd.getId();
        final byte type = cmd.getType();
        // processor here must not be null, for it have been checked before
        UserProcessor processor = ctx.getUserProcessor(cmd.getRequestClass());
        if (processor instanceof AsyncUserProcessor) {
            try {
                //
                processor.handleRequest(processor.preHandleRequest(ctx, cmd.getRequestObject()),
                        new RpcAsyncContext(ctx, cmd, this), cmd.getRequestObject());
            } catch (RejectedExecutionException e) {
                logger
                        .warn("RejectedExecutionException occurred when do ASYNC process in RpcRequestProcessor");
                sendResponseIfNecessary(ctx, type, this.getCommandFactory()
                        .createExceptionResponse(id, ResponseStatus.SERVER_THREADPOOL_BUSY));
            } catch (Throwable t) {
                String errMsg = "AYSNC process rpc request failed in RpcRequestProcessor, id=" + id;
                logger.error(errMsg, t);
                sendResponseIfNecessary(ctx, type, this.getCommandFactory()
                        .createExceptionResponse(id, t, errMsg));
            }
        } else {
            try {
                Object responseObject = processor
                        .handleRequest(processor.preHandleRequest(ctx, cmd.getRequestObject()),
                                cmd.getRequestObject());

                sendResponseIfNecessary(ctx, type,
                        this.getCommandFactory().createResponse(responseObject, cmd));
            } catch (RejectedExecutionException e) {
                logger
                        .warn("RejectedExecutionException occurred when do SYNC process in RpcRequestProcessor");
                sendResponseIfNecessary(ctx, type, this.getCommandFactory()
                        .createExceptionResponse(id, ResponseStatus.SERVER_THREADPOOL_BUSY));
            } catch (Throwable t) {
                String errMsg = "SYNC process rpc request failed in RpcRequestProcessor, id=" + id;
                logger.error(errMsg, t);
                sendResponseIfNecessary(ctx, type, this.getCommandFactory()
                        .createExceptionResponse(id, t, errMsg));
            }
        }
    }

    /**
     * deserialize request command
     *
     * @return true if deserialize success; false if exception catched
     */
    private boolean deserializeRequestCommand(RemotingContext ctx, RpcRequestCommand cmd, int level) {
        boolean result;
        try {
            cmd.deserialize(level);
            result = true;
        } catch (DeserializationException e) {
            logger.error("DeserializationException occurred when process in RpcRequestProcessor, id={}, deserializeLevel={}",
                    cmd.getId(), RpcDeserializeLevel.valueOf(level), e);
            sendResponseIfNecessary(ctx, cmd.getType(), this.getCommandFactory()
                    .createExceptionResponse(cmd.getId(), ResponseStatus.SERVER_DESERIAL_EXCEPTION, e));
            result = false;
        } catch (Throwable t) {
            String errMsg = "Deserialize RpcRequestCommand failed in RpcRequestProcessor, id="
                    + cmd.getId() + ", deserializeLevel=" + level;
            logger.error(errMsg, t);
            sendResponseIfNecessary(ctx, cmd.getType(), this.getCommandFactory()
                    .createExceptionResponse(cmd.getId(), t, errMsg));
            result = false;
        }
        return result;
    }

    /**
     * pre process remoting context, initial some useful infos and pass to biz
     *
     * @param ctx              remoting context
     * @param cmd              rpc request command
     * @param currentTimestamp current timestamp
     */
    private void preProcessRemotingContext(RemotingContext ctx, RpcRequestCommand cmd,
                                           long currentTimestamp) {
        ctx.setArriveTimestamp(cmd.getArriveTime());
        ctx.setTimeout(cmd.getTimeout());
        ctx.setRpcCommandType(cmd.getType());
        ctx.getInvokeContext().putIfAbsent(InvokeContext.BOLT_PROCESS_WAIT_TIME,
                currentTimestamp - cmd.getArriveTime());
    }

    /**
     * print some log when request timeout and discarded in io thread.
     */
    private void timeoutLog(final RpcRequestCommand cmd, long currentTimestamp, RemotingContext ctx) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug(
                            "request id [{}] currenTimestamp [{}] - arriveTime [{}] = server cost [{}] >= timeout value [{}].",
                            cmd.getId(), currentTimestamp, cmd.getArriveTime(),
                            (currentTimestamp - cmd.getArriveTime()), cmd.getTimeout());
        }

        String remoteAddr = "UNKNOWN";
        if (null != ctx) {
            ChannelHandlerContext channelCtx = ctx.getChannelContext();
            Channel channel = channelCtx.channel();
            if (null != channel) {
                remoteAddr = RemotingUtil.parseRemoteAddress(channel);
            }
        }
        logger
                .warn(
                        "Rpc request id[{}], from remoteAddr[{}] stop process, total wait time in queue is [{}], client timeout setting is [{}].",
                        cmd.getId(), remoteAddr, (currentTimestamp - cmd.getArriveTime()), cmd.getTimeout());
    }

    /**
     * print some debug log when receive request
     */
    private void debugLog(RemotingContext ctx, RpcRequestCommand cmd, long currentTimestamp) {
        if (logger.isDebugEnabled()) {
            logger.debug("Rpc request received! requestId={}, from {}", cmd.getId(),
                    RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            logger.debug(
                    "request id {} currenTimestamp {} - arriveTime {} = server cost {} < timeout {}.",
                    cmd.getId(), currentTimestamp, cmd.getArriveTime(),
                    (currentTimestamp - cmd.getArriveTime()), cmd.getTimeout());
        }
    }

    /**
     * Inner process task
     *
     * @author xiaomin.cxm
     * @version $Id: RpcRequestProcessor.java, v 0.1 May 19, 2016 4:01:28 PM xiaomin.cxm Exp $
     */
    class ProcessTask implements Runnable {

        RemotingContext ctx;
        RpcRequestCommand msg;

        public ProcessTask(RemotingContext ctx, RpcRequestCommand msg) {
            this.ctx = ctx;
            this.msg = msg;
        }
        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                RpcRequestProcessor.this.doProcess(ctx, msg);
            } catch (Throwable e) {
                //protect the thread running this task
                String remotingAddress = RemotingUtil.parseRemoteAddress(ctx.getChannelContext()
                        .channel());
                logger
                        .error(
                                "Exception caught when process rpc request command in RpcRequestProcessor, Id="
                                        + msg.getId() + "! Invoke source address is [" + remotingAddress
                                        + "].", e);
            }
        }

    }
}
