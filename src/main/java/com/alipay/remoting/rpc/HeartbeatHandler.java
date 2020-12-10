package com.alipay.remoting.rpc;

import com.alipay.remoting.Connection;
import com.alipay.remoting.Protocol;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Heart beat triggerd.
 *  todo 客户端 IdleStateEvent 事件处理器，进行客户端空闲逻辑处理（SOFABolt 中客户端使用 RpcHeartbeatTrigger 进行空闲处理）
 * @author jiangping
 * @version $Id: SharableHandler.java, v 0.1 2015-12-14 PM3:16:00 tao Exp $
 */
@Sharable
public class HeartbeatHandler extends ChannelDuplexHandler {

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#userEventTriggered(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ProtocolCode protocolCode = ctx.channel().attr(Connection.PROTOCOL).get();
            Protocol protocol = ProtocolManager.getProtocol(protocolCode);

            //调用 HeartbeatTrigger 做真正的心跳处理业务
            protocol.getHeartbeatTrigger().heartbeatTriggered(ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
