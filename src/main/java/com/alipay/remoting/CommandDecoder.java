package com.alipay.remoting;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Decode command.
 *
 * @author jiangping
 * @version $Id: CommandDecoder.java, v 0.1 Mar 10, 2016 11:32:46 AM jiangping Exp $
 */
public interface CommandDecoder {
    /**
     * Decode bytes into object.
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
}
