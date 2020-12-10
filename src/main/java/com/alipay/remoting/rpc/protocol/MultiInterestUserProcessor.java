package com.alipay.remoting.rpc.protocol;

import java.util.List;

/**
 * Support multi-interests feature based on UserProcessor
 * <p>
 * The implementations of this interface don't need to implement the {@link com.alipay.remoting.rpc.protocol.UserProcessor#interest() interest()} method;
 *
 * @author muyun.cyt (muyun.cyt@antfin.com)  2018/7/5   11:19 AM
 */
public interface MultiInterestUserProcessor<T> extends UserProcessor<T> {

    /**
     * A list of the class names of user request.
     * Use String type to avoid classloader problem.
     */
    List<String> multiInterest();

}
