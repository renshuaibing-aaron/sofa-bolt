package com.alipay.remoting.codec;

import com.alipay.remoting.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;

import java.io.Serializable;

/**
 * Protocol code based newEncoder, the main newEncoder for a certain protocol, which is lead by one or multi bytes (magic code).
 * <p>
 * Notice: this is stateless can be noted as {@link io.netty.channel.ChannelHandler.Sharable}
 *编码代理类
 * @author jiangping
 * @version $Id: ProtocolCodeBasedEncoder.java, v 0.1 2015-12-11 PM 7:30:30 tao Exp $
 */
@ChannelHandler.Sharable
public class ProtocolCodeBasedEncoder extends MessageToByteEncoder<Serializable> {

    /**
     * default protocol code
     * default protocol code, 默认是 RpcProtocolV2
     */
    protected ProtocolCode defaultProtocolCode;

    public ProtocolCodeBasedEncoder(ProtocolCode defaultProtocolCode) {
        super();
        this.defaultProtocolCode = defaultProtocolCode;
    }

    /**
     * 编码
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out)
            throws Exception {

        /**
         * todo  复习 netty源码中编码的过程  查看netty中的MessageToByteEncoder的类
         *    判断传入的数据是否是 Serializable 类型（该类型由 MessageToByteEncoder 的泛型指定），
         *    如果不是，直接传播给 pipeline 中的下一个 handler；否则
         *   创建一个 ByteBuf 实例，用于存储最终的编码数据
         */

        // 1. 从 Channel 获取 ProtocolCode 附属属性
        Attribute<ProtocolCode> att = ctx.channel().attr(Connection.PROTOCOL);
        ProtocolCode protocolCode;

        // 如果为 null，使用默认协议 RpcProtocolV2；否则使用附属属性
        if (att == null || att.get() == null) {
            protocolCode = this.defaultProtocolCode;
        } else {
            protocolCode = att.get();
        }

        // 2. 从协议管理器中获取 ProtocolCode 相应的 Protocol 对象
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);

        // 3. 再从协议对象中获取相应的 CommandEncoder 实现类实例
        CommandEncoder encoder = protocol.getEncoder();

        //使用使用 CommandEncoder 实现类实例按照Protocol 私有协议的设计 所介绍的协议规则将数据写入到创建好的 ByteBuf(out)实例中
        encoder.encode(ctx, msg, out);

        /*
         如果原始数据是 ReferenceCounted 实现类，则释放原始数据
         如果 ByteBuf 中有数据了，则传播给 pipeline 中的下一个 handler；
         否则，释放该 ByteBuf 对象，传递一个空的 ByteBuf 给下一个 handler
         */
    }

}
