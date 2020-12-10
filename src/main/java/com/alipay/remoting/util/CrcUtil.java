package com.alipay.remoting.util;

import java.util.zip.CRC32;

/**
 * CRC32 utility.
 *
 * @author jiangping
 * @version $Id: CrcUtil2, v 0.1 2017-06-05 11:29 Timo Exp $
 */
public class CrcUtil {

    private static final ThreadLocal<CRC32> CRC_32_THREAD_LOCAL = new ThreadLocal<CRC32>() {
        @Override
        protected CRC32 initialValue() {
            return new CRC32();
        }
    };

    /**
     * Compute CRC32 code for byte[].
     *
     * @param array
     * @return
     */
    public static final int crc32(byte[] array) {
        if (array != null) {
            return crc32(array, 0, array.length);
        }

        return 0;
    }

    /**
     * Compute CRC32 code for byte[].
     *
     * @param array
     * @param offset
     * @param length
     * @return
     */
    public static final int crc32(byte[] array, int offset, int length) {
        CRC32 crc32 = CRC_32_THREAD_LOCAL.get();
        crc32.update(array, offset, length);
        int ret = (int) crc32.getValue();
        crc32.reset();
        return ret;
    }

}
