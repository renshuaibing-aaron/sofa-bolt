package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;

/**
 * Extends this to process user defined request in ASYNC way.<br>
 * If you want process reqeuest in SYNC way, please extends {@link SyncUserProcessor}.
 *
 * @author xiaomin.cxm
 * @version $Id: AsyncUserProcessor.java, v 0.1 May 16, 2016 8:18:03 PM xiaomin.cxm Exp $
 */
public abstract class AsyncUserProcessor<T> extends AbstractUserProcessor<T> {
    /**
     * unsupported here!
     *
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#handleRequest(com.alipay.remoting.BizContext, java.lang.Object)
     */
    @Override
    public Object handleRequest(BizContext bizCtx, T request) throws Exception {
        throw new UnsupportedOperationException(
                "SYNC handle request is unsupported in AsyncUserProcessor!");
    }

    /**
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#handleRequest(com.alipay.remoting.BizContext, com.alipay.remoting.AsyncContext, java.lang.Object)
     */
    @Override
    public abstract void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request);

    /**
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#interest()
     */
    @Override
    public abstract String interest();
}