package com.alipay.remoting.exception;

/**
 * Exception when serialize failed
 *
 * @author tsui
 * @version $Id: SerializationException.java, v 0.1 2017-07-26 16:12 tsui Exp $
 */
public class SerializationException extends CodecException {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 5668965722686668067L;

    private boolean serverSide = false;

    /**
     * Constructor.
     */
    public SerializationException() {

    }

    /**
     * Constructor.
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     */
    public SerializationException(String message, boolean serverSide) {
        this(message);
        this.serverSide = serverSide;
    }

    /**
     * Constructor.
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     */
    public SerializationException(String message, Throwable cause, boolean serverSide) {
        this(message, cause);
        this.serverSide = serverSide;
    }

    /**
     * Getter method for property <tt>serverSide</tt>.
     *
     * @return property value of serverSide
     */
    public boolean isServerSide() {
        return serverSide;
    }
}