package com.alipay.remoting;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RemotingUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Base remoting capability.
 *
 * @author jiangping
 * @version $Id: BaseRemoting.java, v 0.1 Mar 4, 2016 12:09:56 AM tao Exp $
 */
public abstract class BaseRemoting {
    /**
     * logger
     */
    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");

    protected CommandFactory commandFactory;

    public BaseRemoting(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Synchronous invocation
     *
     * @param conn
     * @param request
     * @param timeoutMillis
     * @return
     * @throws InterruptedException
     * @throws RemotingException
     */
    protected RemotingCommand invokeSync(final Connection conn, final RemotingCommand request, final int timeoutMillis) throws RemotingException,
            InterruptedException {

        //创建InvokeFuture，并将 { invokeId : InvokeFuture实例} 存储到 Connection 的 Map<Integer, InvokeFuture> invokeFutureMap
        final InvokeFuture future = createInvokeFuture(request, request.getInvokeContext());
        conn.addInvokeFuture(future);


        try {
            //使用 netty 发送消息
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        conn.removeInvokeFuture(request.getId());
                        future.putResponse(commandFactory.createSendFailedResponse(
                                conn.getRemoteAddress(), f.cause()));
                        logger.error("Invoke send failed, id={}", request.getId(), f.cause());
                    }
                }

            });
        } catch (Exception e) {
            conn.removeInvokeFuture(request.getId());
            if (future != null) {
                future.putResponse(commandFactory.createSendFailedResponse(conn.getRemoteAddress(),
                        e));
            }
            logger.error("Exception caught when sending invocation, id={}", request.getId(), e);
        }
       //当前线程阻塞在这里，等待响应返回并填充到future后，再进行唤醒
        RemotingCommand response = future.waitResponse(timeoutMillis);

      //
        if (response == null) {
            conn.removeInvokeFuture(request.getId());
            response = this.commandFactory.createTimeoutResponse(conn.getRemoteAddress());
            logger.warn("Wait response, request id={} timeout!", request.getId());
        }

        return response;
    }

    /**
     * Invocation with callback.
     *
     * @param conn
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @throws InterruptedException
     */
    protected void invokeWithCallback(final Connection conn, final RemotingCommand request,
                                      final InvokeCallback invokeCallback, final int timeoutMillis) {
        final InvokeFuture future = createInvokeFuture(conn, request, request.getInvokeContext(),
                invokeCallback);
        conn.addInvokeFuture(future);

        try {
            //add timeout
            Timeout timeout = TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    InvokeFuture future = conn.removeInvokeFuture(request.getId());
                    if (future != null) {
                        future.putResponse(commandFactory.createTimeoutResponse(conn
                                .getRemoteAddress()));
                        future.tryAsyncExecuteInvokeCallbackAbnormally();
                    }
                }

            }, timeoutMillis, TimeUnit.MILLISECONDS);
            future.addTimeout(timeout);
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture cf) throws Exception {
                    if (!cf.isSuccess()) {
                        InvokeFuture f = conn.removeInvokeFuture(request.getId());
                        if (f != null) {
                            f.cancelTimeout();
                            f.putResponse(commandFactory.createSendFailedResponse(
                                    conn.getRemoteAddress(), cf.cause()));
                            f.tryAsyncExecuteInvokeCallbackAbnormally();
                        }
                        logger.error("Invoke send failed. The address is {}",
                                RemotingUtil.parseRemoteAddress(conn.getChannel()), cf.cause());
                    }
                }

            });
        } catch (Exception e) {
            InvokeFuture f = conn.removeInvokeFuture(request.getId());
            if (f != null) {
                f.cancelTimeout();
                f.putResponse(commandFactory.createSendFailedResponse(conn.getRemoteAddress(), e));
                f.tryAsyncExecuteInvokeCallbackAbnormally();
            }
            logger.error("Exception caught when sending invocation. The address is {}",
                    RemotingUtil.parseRemoteAddress(conn.getChannel()), e);
        }
    }

    /**
     * Invocation with future returned.
     *
     * @param conn
     * @param request
     * @param timeoutMillis
     * @return
     */
    protected InvokeFuture invokeWithFuture(final Connection conn, final RemotingCommand request,
                                            final int timeoutMillis) {

        final InvokeFuture future = createInvokeFuture(request, request.getInvokeContext());
        conn.addInvokeFuture(future);
        try {
            //add timeout
            Timeout timeout = TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    InvokeFuture future = conn.removeInvokeFuture(request.getId());
                    if (future != null) {
                        future.putResponse(commandFactory.createTimeoutResponse(conn
                                .getRemoteAddress()));
                    }
                }

            }, timeoutMillis, TimeUnit.MILLISECONDS);
            future.addTimeout(timeout);

            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture cf) throws Exception {
                    if (!cf.isSuccess()) {
                        InvokeFuture f = conn.removeInvokeFuture(request.getId());
                        if (f != null) {
                            f.cancelTimeout();
                            f.putResponse(commandFactory.createSendFailedResponse(
                                    conn.getRemoteAddress(), cf.cause()));
                        }
                        logger.error("Invoke send failed. The address is {}",
                                RemotingUtil.parseRemoteAddress(conn.getChannel()), cf.cause());
                    }
                }

            });
        } catch (Exception e) {
            InvokeFuture f = conn.removeInvokeFuture(request.getId());
            if (f != null) {
                f.cancelTimeout();
                f.putResponse(commandFactory.createSendFailedResponse(conn.getRemoteAddress(), e));
            }
            logger.error("Exception caught when sending invocation. The address is {}",
                    RemotingUtil.parseRemoteAddress(conn.getChannel()), e);
        }
        return future;
    }

    ;

    /**
     * Oneway invocation.
     *
     * @param conn
     * @param request
     * @throws InterruptedException
     */
    protected void oneway(final Connection conn, final RemotingCommand request) {
        try {
            conn.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (!f.isSuccess()) {
                        logger.error("Invoke send failed. The address is {}",
                                RemotingUtil.parseRemoteAddress(conn.getChannel()), f.cause());
                    }
                }

            });
        } catch (Exception e) {
            if (null == conn) {
                logger.error("Conn is null");
            } else {
                logger.error("Exception caught when sending invocation. The address is {}",
                        RemotingUtil.parseRemoteAddress(conn.getChannel()), e);
            }
        }
    }

    ;

    /**
     * Create invoke future with {@link InvokeContext}.
     *
     * @param request
     * @param invokeContext
     * @return
     */
    protected abstract InvokeFuture createInvokeFuture(final RemotingCommand request,
                                                       final InvokeContext invokeContext);

    /**
     * Create invoke future with {@link InvokeContext}.
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @return
     */
    protected abstract InvokeFuture createInvokeFuture(final Connection conn,
                                                       final RemotingCommand request,
                                                       final InvokeContext invokeContext,
                                                       final InvokeCallback invokeCallback);

    protected CommandFactory getCommandFactory() {
        return commandFactory;
    }
}
