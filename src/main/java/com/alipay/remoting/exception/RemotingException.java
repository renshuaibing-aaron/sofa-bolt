package com.alipay.remoting.exception;

/**
 * Exception for default remoting problems
 *
 * @author jiangping
 * @version $Id: RemotingException.java, v 0.1 2015-9-21 PM 4:49:46 tao Exp $
 */
public class RemotingException extends Exception {

    /**
     * For serialization
     */
    private static final long serialVersionUID = 6183635628271812505L;

    /**
     * Constructor.
     */
    public RemotingException() {

    }

    /**
     * Constructor.
     */
    public RemotingException(String message) {
        super(message);
    }

    /**
     * Constructor.
     */
    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }

}
