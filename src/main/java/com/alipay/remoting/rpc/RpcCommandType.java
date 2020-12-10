package com.alipay.remoting.rpc;

/**
 * The type of command in the request/response model.
 *
 * @author jiangping
 * @version $Id: RpcCommandType.java, v 0.1 2015-9-25 AM10:58:16 tao Exp $
 */
public class RpcCommandType {
    /**
     * rpc response
     */
    public static final byte RESPONSE = (byte) 0x00;
    /**
     * rpc request
     */
    public static final byte REQUEST = (byte) 0x01;
    /**
     * rpc oneway request
     */
    public static final byte REQUEST_ONEWAY = (byte) 0x02;
}
