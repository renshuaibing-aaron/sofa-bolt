package com.alipay.remoting.util;

/**
 * Exception to represent the run method of a future task has not been called.
 *
 * @author tsui
 * @version $Id: FutureTaskNotRunYetException.java, v 0.1 2017-07-31 16:29 tsui Exp $
 */
public class FutureTaskNotRunYetException extends Exception {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 2929126204324060632L;

    /**
     * Constructor.
     */
    public FutureTaskNotRunYetException() {
    }

    /**
     * Constructor.
     */
    public FutureTaskNotRunYetException(String message) {
        super(message);
    }

    /**
     * Constructor.
     */
    public FutureTaskNotRunYetException(String message, Throwable cause) {
        super(message, cause);
    }
}