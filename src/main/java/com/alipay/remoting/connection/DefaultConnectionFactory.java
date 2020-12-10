package com.alipay.remoting.connection;

import com.alipay.remoting.codec.Codec;
import com.alipay.remoting.config.ConfigurableInstance;
import io.netty.channel.ChannelHandler;

/**
 * Default connection factory.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-06-20 15:18
 */
public class DefaultConnectionFactory extends AbstractConnectionFactory {

    public DefaultConnectionFactory(Codec codec, ChannelHandler heartbeatHandler,
                                    ChannelHandler handler, ConfigurableInstance configInstance) {
        super(codec, heartbeatHandler, handler, configInstance);
    }
}
