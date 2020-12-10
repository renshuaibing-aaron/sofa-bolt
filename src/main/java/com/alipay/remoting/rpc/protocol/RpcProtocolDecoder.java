package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.codec.ProtocolCodeBasedDecoder;
import io.netty.buffer.ByteBuf;

/**
 * Rpc protocol decoder.
 * 解码器
 * @author tsui
 * @version $Id: RpcProtocolDecoder.java, v 0.1 2018-03-27 19:28 tsui Exp $
 */
public class RpcProtocolDecoder extends ProtocolCodeBasedDecoder {
    public static final int MIN_PROTOCOL_CODE_WITH_VERSION = 2;

    public RpcProtocolDecoder(int protocolCodeLength) {
        super(protocolCodeLength);
    }

    /**
     * 这个什么作用呢
     * @param in input byte buf
     * @return
     */
    @Override
    protected byte decodeProtocolVersion(ByteBuf in) {

        // 恢复到 decode 中开始设置的读指针
        in.resetReaderIndex();
        if (in.readableBytes() >= protocolCodeLength + DEFAULT_PROTOCOL_VERSION_LENGTH) {
            byte rpcProtocolCodeByte = in.readByte();

            // 如果 ProtocolCode>=2，则继续读取 version，否则不读取（即只读取 RpcProtocolV2 及以上的 protocolVersion）
            if (rpcProtocolCodeByte >= MIN_PROTOCOL_CODE_WITH_VERSION) {
                return in.readByte();
            }
        }
        return DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH;
    }
}