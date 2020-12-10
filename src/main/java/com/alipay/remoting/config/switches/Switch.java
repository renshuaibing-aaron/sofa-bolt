package com.alipay.remoting.config.switches;

/**
 * switch interface
 *
 * @author tsui
 * @version $Id: Switch.java, v 0.1 2018-04-08 11:26 tsui Exp $
 */
public interface Switch {
    /**
     * api for user to turn on a feature
     *
     * @param index the switch index of feature
     */
    void turnOn(int index);

    /**
     * api for user to turn off a feature
     *
     * @param index the switch index of feature
     */
    void turnOff(int index);

    /**
     * check switch whether on
     *
     * @param index the switch index of feature
     * @return true if either system setting is on or user setting is on
     */
    boolean isOn(int index);
}