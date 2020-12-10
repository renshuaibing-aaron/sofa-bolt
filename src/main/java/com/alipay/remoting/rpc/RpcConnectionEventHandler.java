package com.alipay.remoting.rpc;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventHandler;
import com.alipay.remoting.config.switches.GlobalSwitch;
import io.netty.channel.ChannelHandlerContext;

/**
 * ConnectionEventHandler for Rpc.
 *  ConnectionEventHandler 实现类，重写了其 channelInactive 方法
 * @author jiangping
 * @version $Id: RpcConnectionEventHandler.java, v 0.1 2015-10-16 PM4:41:29 tao Exp $
 */
public class RpcConnectionEventHandler extends ConnectionEventHandler {

    public RpcConnectionEventHandler() {
        super();
    }

    public RpcConnectionEventHandler(GlobalSwitch globalSwitch) {
        super(globalSwitch);
    }

    /**
     * @see com.alipay.remoting.ConnectionEventHandler#channelInactive(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection conn = ctx.channel().attr(Connection.CONNECTION).get();
        if (conn != null) {
            this.getConnectionManager().remove(conn);
        }
        super.channelInactive(ctx);
    }
}
