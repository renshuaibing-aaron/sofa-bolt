package com.alipay.remoting.rpc;

import com.alipay.remoting.*;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispatch messages to corresponding protocol.
 *
 * @author jiangping
 * @version $Id: RpcHandler.java, v 0.1 2015-12-14 PM4:01:37 tao Exp $
 */
@ChannelHandler.Sharable
public class RpcHandler extends ChannelInboundHandlerAdapter {
    private boolean serverSide;

    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors;

    public RpcHandler() {
        serverSide = false;
    }

    public RpcHandler(ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        serverSide = false;
        this.userProcessors = userProcessors;
    }

    public RpcHandler(boolean serverSide, ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        this.serverSide = serverSide;
        this.userProcessors = userProcessors;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("=====【客户端接收到信息channelRead】========");
        // 每一个请求都会在连接上添加 ProtocolCode 属性  连接的上下文中获取使用的协议的版本 ProtocolCode
        ProtocolCode protocolCode = ctx.channel().attr(Connection.PROTOCOL).get();

        System.out.println("根据 channel 中的附加属性获取相应的 Protocol，之后使用该 Protocol 实例的 CommandHandler 处理消息");
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);
        //创建上下文
        RemotingContext remotingContext = new RemotingContext(ctx, new InvokeContext(), serverSide, userProcessors);
        protocol.getCommandHandler().handleCommand(remotingContext, msg);
    }
}
