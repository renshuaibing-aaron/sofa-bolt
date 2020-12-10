package com.alipay.remoting;

import com.alipay.remoting.util.RemotingUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * default biz context
 *
 * @author xiaomin.cxm
 * @version $Id: DefaultBizContext.java, v 0.1 Jan 7, 2016 10:42:30 AM xiaomin.cxm Exp $
 */
public class DefaultBizContext implements BizContext {
    /**
     * remoting context
     */
    private RemotingContext remotingCtx;

    /**
     * Constructor with RemotingContext
     *
     * @param remotingCtx
     */
    public DefaultBizContext(RemotingContext remotingCtx) {
        this.remotingCtx = remotingCtx;
    }

    /**
     * get remoting context
     *
     * @return RemotingContext
     */
    protected RemotingContext getRemotingCtx() {
        return this.remotingCtx;
    }

    /**
     * @see com.alipay.remoting.BizContext#getRemoteAddress()
     */
    @Override
    public String getRemoteAddress() {
        if (null != this.remotingCtx) {
            ChannelHandlerContext channelCtx = this.remotingCtx.getChannelContext();
            Channel channel = channelCtx.channel();
            if (null != channel) {
                return RemotingUtil.parseRemoteAddress(channel);
            }
        }
        return "UNKNOWN_ADDRESS";
    }

    /**
     * @see com.alipay.remoting.BizContext#getRemoteHost()
     */
    @Override
    public String getRemoteHost() {
        if (null != this.remotingCtx) {
            ChannelHandlerContext channelCtx = this.remotingCtx.getChannelContext();
            Channel channel = channelCtx.channel();
            if (null != channel) {
                return RemotingUtil.parseRemoteIP(channel);
            }
        }
        return "UNKNOWN_HOST";
    }

    /**
     * @see com.alipay.remoting.BizContext#getRemotePort()
     */
    @Override
    public int getRemotePort() {
        if (null != this.remotingCtx) {
            ChannelHandlerContext channelCtx = this.remotingCtx.getChannelContext();
            Channel channel = channelCtx.channel();
            if (null != channel) {
                return RemotingUtil.parseRemotePort(channel);
            }
        }
        return -1;
    }

    /**
     * @see BizContext#getConnection()
     */
    @Override
    public Connection getConnection() {
        if (null != this.remotingCtx) {
            return this.remotingCtx.getConnection();
        }
        return null;
    }

    /**
     * @see com.alipay.remoting.BizContext#isRequestTimeout()
     */
    @Override
    public boolean isRequestTimeout() {
        return this.remotingCtx.isRequestTimeout();
    }

    /**
     * get the timeout value from rpc client.
     *
     * @return
     */
    @Override
    public int getClientTimeout() {
        return this.remotingCtx.getTimeout();
    }

    /**
     * get the arrive time stamp
     *
     * @return
     */
    @Override
    public long getArriveTimestamp() {
        return this.remotingCtx.getArriveTimestamp();
    }

    /**
     * @see com.alipay.remoting.BizContext#put(java.lang.String, java.lang.String)
     */
    @Override
    public void put(String key, String value) {
    }

    /**
     * @see com.alipay.remoting.BizContext#get(java.lang.String)
     */
    @Override
    public String get(String key) {
        return null;
    }

    /**
     * @see BizContext#getInvokeContext()
     */
    @Override
    public InvokeContext getInvokeContext() {
        return this.remotingCtx.getInvokeContext();
    }
}
