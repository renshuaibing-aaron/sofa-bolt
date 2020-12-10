package com.alipay.remoting;

import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RemotingUtil;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;

/**
 * Processor to process remoting command.
 *
 * @param <T>
 * @author jiangping
 * @version $Id: RemotingProcessor.java, v 0.1 2015-9-6 PM2:50:51 tao Exp $
 */
public abstract class AbstractRemotingProcessor<T extends RemotingCommand> implements
        RemotingProcessor<T> {
    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");
    private ExecutorService executor;
    private CommandFactory commandFactory;

    /**
     * Default constructor.
     */
    public AbstractRemotingProcessor() {

    }

    /**
     * Constructor.
     */
    public AbstractRemotingProcessor(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Constructor.
     *
     * @param executor
     */
    public AbstractRemotingProcessor(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Constructor.
     *
     * @param executor
     */
    public AbstractRemotingProcessor(CommandFactory commandFactory, ExecutorService executor) {
        this.commandFactory = commandFactory;
        this.executor = executor;
    }

    /**
     * Do the process.
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    public abstract void doProcess(RemotingContext ctx, T msg) throws Exception;

    /**
     * Process the remoting command with its own executor or with the defaultExecutor if its own if null.
     *
     * @param ctx
     * @param msg
     * @param defaultExecutor
     * @throws Exception
     */
    /**
     * 提供模板方法
     * 对于RpcResponseProcessor和RpcHeartBeatProcessor，如果RemotingProcessor#executor为null，就使用ProcessorManager#defaultExecutor；
     * 对于RpcRequestProcessor，会经历通过UserProcessor#executorSelector选择线程池 -> UserProcessor#executor -> RemotingProcessor#executor -> ProcessorManager#defaultExecutor
     */
    @Override
    public void process(RemotingContext ctx, T msg, ExecutorService defaultExecutor)
            throws Exception {

        ProcessTask task = new ProcessTask(ctx, msg);
        //如果 RemotingProcessor 自定义了线程池 executor 执行 ProcessTask.run()，否则使用 ProcessorManager 的 defaultExecutor
        if (this.getExecutor() != null) {
            this.getExecutor().execute(task);
        } else {
            defaultExecutor.execute(task);
        }
    }

    /**
     * Getter method for property <tt>executor</tt>.
     *
     * @return property value of executor
     */
    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Setter method for property <tt>executor</tt>.
     *
     * @param executor value to be assigned to property executor
     */
    @Override
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Task for asynchronous process.
     *
     * @author jiangping
     * @version $Id: RemotingProcessor.java, v 0.1 2015-10-14 PM7:40:44 tao Exp $
     */
    class ProcessTask implements Runnable {

        RemotingContext ctx;
        T msg;

        public ProcessTask(RemotingContext ctx, T msg) {
            this.ctx = ctx;
            this.msg = msg;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {

                AbstractRemotingProcessor.this.doProcess(ctx, msg);
            } catch (Throwable e) {
                //protect the thread running this task
                String remotingAddress = RemotingUtil.parseRemoteAddress(ctx.getChannelContext()
                        .channel());
                logger
                        .error(
                                "Exception caught when process rpc request command in AbstractRemotingProcessor, Id="
                                        + msg.getId() + "! Invoke source address is [" + remotingAddress
                                        + "].", e);
            }
        }

    }

}
