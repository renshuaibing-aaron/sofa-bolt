package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;

/**
 * Extends this to process user defined request in SYNC way.<br>
 * If you want process reqeuest in ASYNC way, please extends {@link AsyncUserProcessor}.
 *
 * @author xiaomin.cxm
 * @version $Id: SyncUserProcessor.java, v 0.1 May 19, 2016 2:47:21 PM xiaomin.cxm Exp $
 */
public abstract class SyncUserProcessor<T> extends AbstractUserProcessor<T> {
    /**
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#handleRequest(com.alipay.remoting.BizContext, java.lang.Object)
     */
    @Override
    public abstract Object handleRequest(BizContext bizCtx, T request) throws Exception;

    /**
     * unsupported here!
     *
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#handleRequest(com.alipay.remoting.BizContext, com.alipay.remoting.AsyncContext, java.lang.Object)
     */
    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request) {
        throw new UnsupportedOperationException(
                "ASYNC handle request is unsupported in SyncUserProcessor!");
    }

    /**
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#interest()
     */
    @Override
    public abstract String interest();
}
