package com.alipay.remoting;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manager of all protocols
 * 协议容器
 *
 * @author tsui
 * @version $Id: ProtocolManager.java, v 0.1 2018-03-27 15:18 tsui Exp $
 */
public class ProtocolManager {

    // 协议容器 这里就是协议信息
    private static final ConcurrentMap<ProtocolCode, Protocol> protocols = new ConcurrentHashMap<ProtocolCode, Protocol>();

    public static Protocol getProtocol(ProtocolCode protocolCode) {
        return protocols.get(protocolCode);
    }

    public static void registerProtocol(Protocol protocol, byte... protocolCodeBytes) {
        registerProtocol(protocol, ProtocolCode.fromBytes(protocolCodeBytes));
    }

    public static void registerProtocol(Protocol protocol, ProtocolCode protocolCode) {
        if (null == protocolCode || null == protocol) {
            throw new RuntimeException("Protocol: " + protocol + " and protocol code:"
                    + protocolCode + " should not be null!");
        }
        Protocol exists = ProtocolManager.protocols.putIfAbsent(protocolCode, protocol);
        if (exists != null) {
            throw new RuntimeException("Protocol for code: " + protocolCode + " already exists!");
        }
    }

    public static Protocol unRegisterProtocol(byte protocolCode) {
        return ProtocolManager.protocols.remove(ProtocolCode.fromBytes(protocolCode));
    }
}