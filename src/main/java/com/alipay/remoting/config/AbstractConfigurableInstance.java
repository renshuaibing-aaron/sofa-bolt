package com.alipay.remoting.config;

import com.alipay.remoting.config.configs.ConfigContainer;
import com.alipay.remoting.config.configs.ConfigItem;
import com.alipay.remoting.config.configs.ConfigType;
import com.alipay.remoting.config.configs.DefaultConfigContainer;
import com.alipay.remoting.config.switches.GlobalSwitch;

/**
 * common implementation for a configurable instance
 *
 * @author tsui
 * @version $Id: AbstractConfigurableInstance.java, v 0.1 2018-07-30 21:11 tsui Exp $$
 */
public class AbstractConfigurableInstance implements ConfigurableInstance {
    private ConfigContainer configContainer = new DefaultConfigContainer();
    private GlobalSwitch globalSwitch = new GlobalSwitch();
    private ConfigType configType;

    protected AbstractConfigurableInstance(ConfigType configType) {
        this.configType = configType;
    }

    @Override
    public ConfigContainer conf() {
        return this.configContainer;
    }

    @Override
    public GlobalSwitch switches() {
        return this.globalSwitch;
    }

    @Override
    public void initWriteBufferWaterMark(int low, int high) {
        this.configContainer.set(configType, ConfigItem.NETTY_BUFFER_LOW_WATER_MARK, low);
        this.configContainer.set(configType, ConfigItem.NETTY_BUFFER_HIGH_WATER_MARK, high);
    }

    @Override
    public int netty_buffer_low_watermark() {
        if (null != configContainer
                && configContainer.contains(configType, ConfigItem.NETTY_BUFFER_LOW_WATER_MARK)) {
            return (Integer) configContainer
                    .get(configType, ConfigItem.NETTY_BUFFER_LOW_WATER_MARK);
        } else {
            return ConfigManager.netty_buffer_low_watermark();
        }
    }

    @Override
    public int netty_buffer_high_watermark() {
        if (null != configContainer
                && configContainer.contains(configType, ConfigItem.NETTY_BUFFER_HIGH_WATER_MARK)) {
            return (Integer) configContainer.get(configType,
                    ConfigItem.NETTY_BUFFER_HIGH_WATER_MARK);
        } else {
            return ConfigManager.netty_buffer_high_watermark();
        }
    }
}