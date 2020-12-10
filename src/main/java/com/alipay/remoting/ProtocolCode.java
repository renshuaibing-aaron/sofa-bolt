package com.alipay.remoting;

import java.util.Arrays;

/**
 * Protocol code definition, you can define your own protocol code in byte array {@link ProtocolCode#version}
 * We suggest to use just one byte for simplicity.
 *
 * @author tsui
 * @version $Id: ProtocolCode.java, v 0.1 2018-03-27 17:23 tsui Exp $
 */
public class ProtocolCode {
    /**
     * bytes to represent protocol code
     */
    byte[] version;

    private ProtocolCode(byte... version) {
        this.version = version;
    }

    public static ProtocolCode fromBytes(byte... version) {
        return new ProtocolCode(version);
    }

    /**
     * get the first single byte if your protocol code is single code.
     *
     * @return
     */
    public byte getFirstByte() {
        return this.version[0];
    }

    public int length() {
        return this.version.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtocolCode that = (ProtocolCode) o;
        return Arrays.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(version);
    }

    @Override
    public String toString() {
        return "ProtocolVersion{" + "version=" + Arrays.toString(version) + '}';
    }
}