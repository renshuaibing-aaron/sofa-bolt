package com.alipay.remoting.codec;

import io.netty.channel.ChannelHandler;

/**
 * Codec interface.
 *编解码器工厂类接口
 * @author chengyi (mark.lx@antfin.com) 2018-06-20 21:07
 */
public interface Codec {

    /**
     * Create an encoder instance.
     *
     * @return new encoder instance
     */
    ChannelHandler newEncoder();

    /**
     * Create an decoder instance.
     *
     * @return new decoder instance
     */
    ChannelHandler newDecoder();
}
