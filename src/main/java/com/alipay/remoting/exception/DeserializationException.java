package com.alipay.remoting.exception;

/**
 * Exception when deserialize failed
 *
 * @author tsui
 * @version $Id: DeserializationException.java, v 0.1 2017-07-26 16:13 tsui Exp $
 */
public class DeserializationException extends CodecException {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 310446237157256052L;

    private boolean serverSide = false;

    /**
     * Constructor.
     */
    public DeserializationException() {

    }

    /**
     * Constructor.
     */
    public DeserializationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     */
    public DeserializationException(String message, boolean serverSide) {
        this(message);
        this.serverSide = serverSide;
    }

    /**
     * Constructor.
     */
    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     */
    public DeserializationException(String message, Throwable cause, boolean serverSide) {
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