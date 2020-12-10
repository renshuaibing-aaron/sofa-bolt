package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;

import java.util.List;

/**
 * Extends this to process user defined request in SYNC way.<br>
 * If you want process reqeuest in ASYNC way, please extends {@link AsynMultiInterestUserProcessor}.
 *
 * @author muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:19 AM
 */
public abstract class SyncMutiInterestUserProcessor<T> extends
        AbstractMultiInterestUserProcessor<T> {

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
                "ASYNC handle request is unsupported in SyncMutiInterestUserProcessor!");
    }

    /**
     * @see com.alipay.remoting.rpc.protocol.MultiInterestUserProcessor#multiInterest()
     */
    @Override
    public abstract List<String> multiInterest();

}
