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
package com.alipay.remoting.codec;

import com.alipay.remoting.Connection;
import com.alipay.remoting.Protocol;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.ProtocolManager;
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

        // // 1. 从 Channel 获取 ProtocolCode 附属属性
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
        protocol.getEncoder().encode(ctx, msg, out);
    }

}
