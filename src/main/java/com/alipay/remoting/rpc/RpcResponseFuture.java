package com.alipay.remoting.rpc;

import com.alipay.remoting.InvokeFuture;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.exception.InvokeTimeoutException;

/**
 * The future for response.
 *
 * @author jiangping
 * @version $Id: ResponseFuture.java, v 0.1 2015-10-3 PM5:07:05 tao Exp $
 */
public class RpcResponseFuture {
    /**
     * rpc server address
     */
    private String addr;

    /**
     * rpc server port
     */
    private InvokeFuture future;

    /**
     * Constructor
     */
    public RpcResponseFuture(String addr, InvokeFuture future) {
        this.addr = addr;
        this.future = future;
    }

    /**
     * Whether the future is done.
     */
    public boolean isDone() {
        return this.future.isDone();
    }

    /**
     * get result with timeout specified
     * <p>
     * if request done, resolve normal responseObject
     * if request not done, throws InvokeTimeoutException
     */
    public Object get(int timeoutMillis) throws InvokeTimeoutException, RemotingException,
            InterruptedException {
        this.future.waitResponse(timeoutMillis);
        if (!isDone()) {
            throw new InvokeTimeoutException("Future get result timeout!");
        }
        ResponseCommand responseCommand = (ResponseCommand) this.future.waitResponse();
        responseCommand.setInvokeContext(this.future.getInvokeContext());
        return RpcResponseResolver.resolveResponseObject(responseCommand, addr);
    }

    public Object get() throws RemotingException, InterruptedException {
        ResponseCommand responseCommand = (ResponseCommand) this.future.waitResponse();
        responseCommand.setInvokeContext(this.future.getInvokeContext());
        return RpcResponseResolver.resolveResponseObject(responseCommand, addr);
    }

}
