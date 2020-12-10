package com.alipay.remoting.config.configs;

/**
 * netty related configuration items
 *
 * @author tsui
 * @version $Id: NettyConfigure.java, v 0.1 2018-07-30 21:42 tsui Exp $$
 */
public interface NettyConfigure {
    /**
     * Initialize netty write buffer water mark for remoting instance.
     * <p>
     * Notice: This api should be called before init remoting instance.
     *
     * @param low  [0, high]
     * @param high [high, Integer.MAX_VALUE)
     */
    void initWriteBufferWaterMark(int low, int high);

    /**
     * get the low water mark for netty write buffer
     *
     * @return low watermark
     */
    int netty_buffer_low_watermark();

    /**
     * get the high water mark for netty write buffer
     *
     * @return high watermark
     */
    int netty_buffer_high_watermark();
}