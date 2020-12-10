package com.alipay.remoting.config.configs;

/**
 * Items of config.
 * <p>
 * Mainly used to define some config items managed by {@link ConfigContainer}.
 * You can define new config items based on this if need.
 *
 * @author tsui
 * @version $Id: ConfigItem.java, v 0.1 2018-07-28 17:43 tsui Exp $$
 */
public enum ConfigItem {
    // ~~~ netty related
    NETTY_BUFFER_LOW_WATER_MARK, // netty writer buffer low water mark
    NETTY_BUFFER_HIGH_WATER_MARK // netty writer buffer high water mark
}