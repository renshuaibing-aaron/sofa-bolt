package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;

import java.util.List;

/**
 * Extends this to process user defined request in ASYNC way.<br>
 * If you want process reqeuest in SYNC way, please extends {@link SyncMutiInterestUserProcessor}.
 *
 * @author muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:19 AM
 */
public abstract class AsynMultiInterestUserProcessor<T> extends
        AbstractMultiInterestUserProcessor<T> {
    /**
     * unsupported here!
     *
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#handleRequest(com.alipay.remoting.BizContext, java.lang.Object)
     */
    @Override
    public Object handleRequest(BizContext bizCtx, T request) throws Exception {
        throw new UnsupportedOperationException(
                "SYNC handle request is unsupported in AsynMultiInterestUserProcessor!");
    }

    /**
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#handleRequest(com.alipay.remoting.BizContext, com.alipay.remoting.AsyncContext, java.lang.Object)
     */
    @Override
    public abstract void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request);

    /**
     * @see com.alipay.remoting.rpc.protocol.MultiInterestUserProcessor#multiInterest()
     */
    @Override
    public abstract List<String> multiInterest();
}
