package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.ProtocolManager;

/**
 * Protocol manager.
 *
 * @author tsui
 * @version $Id: RpcProtocols.java, v 0.1 2018-03-27 19:42 tsui Exp $
 */
public class RpcProtocolManager {
    public static final int DEFAULT_PROTOCOL_CODE_LENGTH = 1;

    public static void initProtocols() {
        //将 RpcProtocol 实例添加到 RpcProtocolManager 的 Map<ProtocolCode, Protocol> protocols 中
        ProtocolManager.registerProtocol(new RpcProtocol(), RpcProtocol.PROTOCOL_CODE);

        //将 RpcProtocolV2 实例添加到 RpcProtocolManager 的 Map<ProtocolCode, Protocol> protocols 中
        ProtocolManager.registerProtocol(new RpcProtocolV2(), RpcProtocolV2.PROTOCOL_CODE);
    }
}