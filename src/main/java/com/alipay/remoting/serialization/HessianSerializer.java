package com.alipay.remoting.serialization;

import com.alipay.hessian.ClassNameResolver;
import com.alipay.hessian.internal.InternalNameBlackListFilter;
import com.alipay.remoting.exception.CodecException;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Hessian2 serializer.
 *
 * @author jiangping
 * @version $Id: HessianSerializer.java, v 0.1 2015-10-4 PM9:51:55 tao Exp $
 */
public class HessianSerializer implements Serializer {

    private SerializerFactory serializerFactory = new SerializerFactory();

    public HessianSerializer() {
        //initialize with default black list in hessian
        ClassNameResolver resolver = new ClassNameResolver();
        resolver.addFilter(new InternalNameBlackListFilter(8192));
        serializerFactory.setClassNameResolver(resolver);
    }

    /**
     * @see com.alipay.remoting.serialization.Serializer#serialize(java.lang.Object)
     */
    @Override
    public byte[] serialize(Object obj) throws CodecException {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArray);
        output.setSerializerFactory(serializerFactory);
        try {
            output.writeObject(obj);
            output.close();
        } catch (IOException e) {
            throw new CodecException("IOException occurred when Hessian serializer encode!", e);
        }

        byte[] bytes = byteArray.toByteArray();
        return bytes;
    }

    /**
     * @see com.alipay.remoting.serialization.Serializer#deserialize(byte[], java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] data, String classOfT) throws CodecException {
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(data));
        input.setSerializerFactory(serializerFactory);
        Object resultObject;
        try {
            resultObject = input.readObject();
            input.close();
        } catch (IOException e) {
            throw new CodecException("IOException occurred when Hessian serializer decode!", e);
        }
        return (T) resultObject;
    }

}
