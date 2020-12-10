package com.alipay.remoting.exception;

/**
 * Exception when codec problems occur
 *
 * @author xiaomin.cxm
 * @version $Id: CodecException.java, v 0.1 2016-1-3 PM 6:26:12 xiaomin.cxm Exp $
 */
public class CodecException extends RemotingException {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -7513762648815278960L;

    /**
     * Constructor.
     */
    public CodecException() {
    }

    /**
     * Constructor.
     *
     * @param message the detail message.
     */
    public CodecException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

}