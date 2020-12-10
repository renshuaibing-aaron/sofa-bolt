package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.DefaultBizContext;
import com.alipay.remoting.RemotingContext;

import java.util.concurrent.Executor;

/**
 * Implements common function and provide default value.
 *
 * @author xiaomin.cxm
 * @version $Id: AbstractUserProcessor.java, v 0.1 May 19, 2016 3:38:22 PM xiaomin.cxm Exp $
 */
public abstract class AbstractUserProcessor<T> implements UserProcessor<T> {

    /**
     * executor selector, default null unless provide one using its setter method
     */
    protected ExecutorSelector executorSelector;

    /**
     * Provide a default - {@link DefaultBizContext} implementation of {@link BizContext}.
     *
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#preHandleRequest(com.alipay.remoting.RemotingContext, java.lang.Object)
     */
    @Override
    public BizContext preHandleRequest(RemotingContext remotingCtx, T request) {
        //构造用户业务上下文
        return new DefaultBizContext(remotingCtx);
    }

    /**
     * By default return null.
     *
     * @see UserProcessor#getExecutor()
     */
    @Override
    public Executor getExecutor() {
        return null;
    }

    /**
     * @see UserProcessor#getExecutorSelector()
     */
    @Override
    public ExecutorSelector getExecutorSelector() {
        return this.executorSelector;
    }

    /**
     * @see UserProcessor#setExecutorSelector(ExecutorSelector)
     */
    @Override
    public void setExecutorSelector(ExecutorSelector executorSelector) {
        this.executorSelector = executorSelector;
    }

    /**
     * By default, return false, means not deserialize and process biz logic in io thread
     *
     * @see UserProcessor#processInIOThread()
     */
    @Override
    public boolean processInIOThread() {
        return false;
    }

    /**
     * By default, return true, means discard requests which timeout already.
     */
    @Override
    public boolean timeoutDiscard() {
        return true;
    }
}