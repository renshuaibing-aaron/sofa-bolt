package com.alipay.remoting.codec;

import com.alipay.remoting.Connection;
import com.alipay.remoting.Protocol;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import com.alipay.remoting.exception.CodecException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Protocol code based decoder, the main decoder for a certain protocol, which is lead by one or multi bytes (magic code).
 * <p>
 * Notice: this is not stateless, can not be noted as {@link io.netty.channel.ChannelHandler.Sharable}
 *解码代理类
 * @author xiaomin.cxm
 * @version $Id: ProtocolCodeBasedDecoder.java, v0.1 Mar 20, 2017 2:42:46 PM xiaomin.cxm Exp $
 */
public class ProtocolCodeBasedDecoder extends AbstractBatchDecoder {
    /**
     * by default, suggest design a single byte for protocol version.
     */
    public static final int DEFAULT_PROTOCOL_VERSION_LENGTH = 1;
    /**
     * protocol version should be a positive number, we use -1 to represent illegal
     */
    public static final int DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH = -1;

    /**
     * the length of protocol code  默认是1
     */
    protected int protocolCodeLength;

    public ProtocolCodeBasedDecoder(int protocolCodeLength) {
        super();
        this.protocolCodeLength = protocolCodeLength;
    }

    /**
     * decode the protocol code
     *解码出 protocol_code
     * @param in input byte buf
     * @return an instance of ProtocolCode
     */
    protected ProtocolCode decodeProtocolCode(ByteBuf in) {
        // 从 in 的 readerIndex 开始读取 protocolCodeBytes.length（默认为 1）个字节
        // 即只解码 protocolCode
        if (in.readableBytes() >= protocolCodeLength) {
            byte[] protocolCodeBytes = new byte[protocolCodeLength];
            in.readBytes(protocolCodeBytes);
            return ProtocolCode.fromBytes(protocolCodeBytes);
        }
        return null;
    }

    /**
     * decode the protocol version
     *
     * @param in input byte buf
     * @return a byte to represent protocol version
     */
    protected byte decodeProtocolVersion(ByteBuf in) {
        if (in.readableBytes() >= DEFAULT_PROTOCOL_VERSION_LENGTH) {
            return in.readByte();
        }
        return DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        System.out.println("=============服务端进行解码====1111====================");
        // 标记当前的读指针
        in.markReaderIndex();

        // 解码出 protocol_code
        ProtocolCode protocolCode = decodeProtocolCode(in);
        if (null != protocolCode) {

            // 解码出 protocol_version
            byte protocolVersion = decodeProtocolVersion(in);
            if (ctx.channel().attr(Connection.PROTOCOL).get() == null) {
                ctx.channel().attr(Connection.PROTOCOL).set(protocolCode);
                if (DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH != protocolVersion) {
                    ctx.channel().attr(Connection.VERSION).set(protocolVersion);
                }
            }
            // 根据 protocolCode 获取 Protocol
            Protocol protocol = ProtocolManager.getProtocol(protocolCode);
            if (null != protocol) {

                // todo 恢复读指针到解析 protocolCode 之前
                in.resetReaderIndex();
                // 从通信协议里面获取解码器类进行实际解码
                protocol.getDecoder().decode(ctx, in, out);
            } else {
                throw new CodecException("Unknown protocol code: [" + protocolCode
                        + "] while decode in ProtocolDecoder.");
            }
        }
    }
}
