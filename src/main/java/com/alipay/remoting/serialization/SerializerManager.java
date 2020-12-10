package com.alipay.remoting.serialization;

/**
 * Manage all serializers.
 * <p>
 * Notice: Serializer is different with Codec.
 * Serializer is mainly used to deserialize bytes to object, or serialize object to bytes. We can use hessian, json, protocol buff etc.
 * Codec mainly used to encode bytes or decode bytes according to the protocol format. We can use {@link com.alipay.remoting.codec.ProtocolCodeBasedEncoder} or {@link io.netty.handler.codec.LengthFieldBasedFrameDecoder} etc.
 *  序列化管理器
 *  SerializerManager 是 Serializer 实现类的管理器，通过一个 Serializer[] 存储各种序列化器，
 *  数组索引下标 index 就是 Serializer 的 key，例如 HessianSerializer 的 index = 1
 * @author jiangping
 * @version $Id: SerializerManager.java, v 0.1 2015-9-28 PM3:55:59 tao Exp $
 */
public class SerializerManager {

    public static final byte Hessian2 = 1;
    //这里比较一个骚操作是 这里用的是数组 ，可以有较大的提升
    private static Serializer[] serializers = new Serializer[5];
    //public static final byte    Json        = 2;

    static {
        addSerializer(Hessian2, new HessianSerializer());
    }

    public static Serializer getSerializer(int idx) {
        return serializers[idx];
    }

    public static void addSerializer(int idx, Serializer serializer) {
        if (serializers.length <= idx) {
            Serializer[] newSerializers = new Serializer[idx + 5];
            System.arraycopy(serializers, 0, newSerializers, 0, serializers.length);
            serializers = newSerializers;
        }
        serializers[idx] = serializer;
    }
}
