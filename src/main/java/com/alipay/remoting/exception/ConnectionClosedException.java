package com.alipay.remoting.exception;

/**
 * Exception when connection is closed.
 *
 * @author jiangping
 * @version $Id: ConnectionClosedException.java, v 0.1 Jan 15, 2016 3:13:12 PM tao Exp $
 */
public class ConnectionClosedException extends RemotingException {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -2595820033346329315L;

    /**
     * Default constructor.
     */
    public ConnectionClosedException() {
    }

    public ConnectionClosedException(String msg) {
        super(msg);
    }

    public ConnectionClosedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
