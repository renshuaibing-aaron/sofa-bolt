package com.alipay.remoting.config;

import com.alipay.remoting.config.configs.ConfigContainer;
import com.alipay.remoting.config.configs.NettyConfigure;
import com.alipay.remoting.config.switches.GlobalSwitch;

/**
 * define an interface which can be used to implement configurable apis.
 *
 * @author tsui
 * @version $Id: ConfigurableInstance.java, v 0.1 2018-07-30 21:09 tsui Exp $$
 */
public interface ConfigurableInstance extends NettyConfigure {

    /**
     * get the config container for current instance
     *
     * @return the config container
     */
    ConfigContainer conf();

    /**
     * get the global switch for current instance
     *
     * @return the global switch
     */
    GlobalSwitch switches();
}