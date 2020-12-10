package com.alipay.remoting.serialization;

import com.alipay.remoting.exception.CodecException;

/**
 * Serializer for serialize and deserialize.
 * 序列化接口  有一个默认的序列化接口HessianSerializer
 * Serializer 定义了序列化接口，提供了一个默认实现 HessianSerializer，我们可以通过模仿 HessianSerializer 实现 Serializer 接口来提供自己的序列化方式
 * @author jiangping
 * @version $Id: Serializer.java, v 0.1 2015-10-4 PM9:37:57 tao Exp $
 */
public interface Serializer {
    /**
     * Encode object into bytes.
     *
     * @param obj target object
     * @return serialized result
     */
    byte[] serialize(final Object obj) throws CodecException;

    /**
     * Decode bytes into Object.
     *
     * @param data     serialized data
     * @param classOfT class of original data
     */
    <T> T deserialize(final byte[] data, String classOfT) throws CodecException;
}
