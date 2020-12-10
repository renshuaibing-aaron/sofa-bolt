package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.RemotingContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Async biz context of Rpc.
 *
 * @author xiaomin.cxm
 * @version $Id: RpcAsyncContext.java, v 0.1 May 16, 2016 8:23:07 PM xiaomin.cxm Exp $
 */
public class RpcAsyncContext implements AsyncContext {
    /**
     * remoting context
     */
    private RemotingContext ctx;

    /**
     * rpc request command
     */
    private RpcRequestCommand cmd;

    private RpcRequestProcessor processor;

    /**
     * is response sent already
     */
    private AtomicBoolean isResponseSentAlready = new AtomicBoolean();

    /**
     * Default constructor.
     *
     * @param ctx       remoting context
     * @param cmd       rpc request command
     * @param processor rpc request processor
     */
    public RpcAsyncContext(final RemotingContext ctx, final RpcRequestCommand cmd,
                           final RpcRequestProcessor processor) {
        this.ctx = ctx;
        this.cmd = cmd;
        this.processor = processor;
    }

    /**
     * @see com.alipay.remoting.AsyncContext#sendResponse(java.lang.Object)
     */
    @Override
    public void sendResponse(Object responseObject) {
        if (isResponseSentAlready.compareAndSet(false, true)) {
            processor.sendResponseIfNecessary(this.ctx, cmd.getType(), processor
                    .getCommandFactory().createResponse(responseObject, this.cmd));
        } else {
            throw new IllegalStateException("Should not send rpc response repeatedly!");
        }
    }
}