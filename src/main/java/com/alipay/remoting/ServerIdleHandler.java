package com.alipay.remoting;

import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RemotingUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;

/**
 * Server Idle handler.
 * 服务端 IdleStateEvent 事件处理器，进行服务端空闲逻辑处理（SOFABolt 中服务端直接关闭连接）
 * <p>
 * In the server side, the connection will be closed if it is idle for a certain period of time.
 *
 * @author jiangping
 * @version $Id: ServerIdleHandler.java, v 0.1 Nov 3, 2015 05:23:19 PM tao Exp $
 */
@Sharable
public class ServerIdleHandler extends ChannelDuplexHandler {

    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");

    /**
     * @see io.netty.channel.ChannelInboundHandlerAdapter#userEventTriggered(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            try {
                logger.warn("Connection idle, close it from server side: {}",
                        RemotingUtil.parseRemoteAddress(ctx.channel()));

                //直接关闭链接
                ctx.close();
            } catch (Exception e) {
                logger.warn("Exception caught when closing connection in ServerIdleHandler.", e);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
