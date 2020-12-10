package com.alipay.remoting;

import java.util.concurrent.ExecutorService;

/**
 * Remoting processor processes remoting commands.
 *RemotingProcessor 命令处理器的设计：真正的 RemotingCommand 处理器
 * @author jiangping
 * @version $Id: RemotingProcessor.java, v 0.1 Dec 22, 2015 11:48:43 AM tao Exp $
 */
public interface RemotingProcessor<T extends RemotingCommand> {

    /**
     * Process the remoting command.
     *
     * @param ctx
     * @param msg
     * @param defaultExecutor
     * @throws Exception
     */
    void process(RemotingContext ctx, T msg, ExecutorService defaultExecutor) throws Exception;

    /**
     * Get the executor.
     *
     * @return
     */
    ExecutorService getExecutor();

    /**
     * Set executor.
     *
     * @param executor
     */
    void setExecutor(ExecutorService executor);

}
