/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

        System.out.println("====================RpcHandler#channelRead=============");
        // 每一个请求都会在连接上添加 ProtocolCode 属性
        ProtocolCode protocolCode = ctx.channel().attr(Connection.PROTOCOL).get();

        System.out.println("根据 channel 中的附加属性获取相应的 Protocol，之后使用该 Protocol 实例的 CommandHandler 处理消息");
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);

        //创建上下文
        RemotingContext remotingContext = new RemotingContext(ctx, new InvokeContext(), serverSide, userProcessors);


        protocol.getCommandHandler().handleCommand(remotingContext, msg);
    }
}
