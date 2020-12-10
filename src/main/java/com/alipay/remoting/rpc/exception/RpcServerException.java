package com.alipay.remoting.rpc.exception;

import com.alipay.remoting.exception.RemotingException;

/**
 * Rpc server exception when processing request
 *
 * @author jiangping
 * @version $Id: InvokeServerException.java, v 0.1 2015-10-9 PM11:16:10 tao Exp $
 */
public class RpcServerException extends RemotingException {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 4480283862377034355L;

    /**
     * Default constructor.
     */
    public RpcServerException() {
    }

    public RpcServerException(String msg) {
        super(msg);
    }

    public RpcServerException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
