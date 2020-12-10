package com.alipay.remoting;

import io.netty.channel.ChannelHandlerContext;

/**
 * Heartbeat triggers here.
 *心跳触发器
 * @author jiangping
 * @version $Id: HeartbeatTrigger.java, v 0.1 2015-12-14 PM3:40:38 tao Exp $
 */
public interface HeartbeatTrigger {
    void heartbeatTriggered(final ChannelHandlerContext ctx) throws Exception;
}
