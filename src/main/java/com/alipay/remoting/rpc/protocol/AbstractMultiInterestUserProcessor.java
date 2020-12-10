package com.alipay.remoting.rpc.protocol;

/**
 * Implements common function and provide default value.
 * more details in {@link com.alipay.remoting.rpc.protocol.AbstractUserProcessor}
 *
 * @author muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:17 AM
 */
public abstract class AbstractMultiInterestUserProcessor<T> extends AbstractUserProcessor<T>
        implements
        MultiInterestUserProcessor<T> {

    /**
     * do not need to implement this method because of the multiple interests
     *
     * @see com.alipay.remoting.rpc.protocol.UserProcessor#interest()
     */
    @Override
    public String interest() {
        return null;
    }

}
