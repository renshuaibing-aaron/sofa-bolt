package com.alipay.remoting.config.configs;

/**
 * type of config
 *
 * @author tsui
 * @version $Id: ConfigType.java, v 0.1 2018-07-28 17:41 tsui Exp $$
 */
public enum ConfigType {
    CLIENT_SIDE, // configs of this type can only be used in client side
    SERVER_SIDE // configs of this type can only be used in server side
}