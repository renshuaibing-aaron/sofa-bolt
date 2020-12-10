package com.alipay.remoting;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.rpc.ResponseCommand;

/**
 * Define custom serializers for command header and content.
 *自定义序列化器
 * @author jiangping
 * @version $Id: CustomSerializer.java, v 0.1 2015-10-7 AM11:37:36 tao Exp $
 */
public interface CustomSerializer {
    /**
     * Serialize the header of RequestCommand.
     *
     * @param request
     * @param invokeContext
     * @return
     * @throws CodecException
     */
    <T extends RequestCommand> boolean serializeHeader(T request, InvokeContext invokeContext)
            throws SerializationException;

    /**
     * Serialize the header of ResponseCommand.
     *
     * @param response
     * @return
     * @throws CodecException
     */
    <T extends ResponseCommand> boolean serializeHeader(T response) throws SerializationException;

    /**
     * Deserialize the header of RequestCommand.
     *
     * @param request
     * @return
     * @throws CodecException
     */
    <T extends RequestCommand> boolean deserializeHeader(T request) throws DeserializationException;

    /**
     * Deserialize the header of ResponseCommand.
     *
     * @param response
     * @param invokeContext
     * @return
     * @throws CodecException
     */
    <T extends ResponseCommand> boolean deserializeHeader(T response, InvokeContext invokeContext)
            throws DeserializationException;

    /**
     * Serialize the content of RequestCommand.
     *
     * @param request
     * @param invokeContext
     * @return
     * @throws CodecException
     */
    <T extends RequestCommand> boolean serializeContent(T request, InvokeContext invokeContext)
            throws SerializationException;

    /**
     * Serialize the content of ResponseCommand.
     *
     * @param response
     * @return
     * @throws CodecException
     */
    <T extends ResponseCommand> boolean serializeContent(T response) throws SerializationException;

    /**
     * Deserialize the content of RequestCommand.
     *
     * @param request
     * @return
     * @throws CodecException
     */
    <T extends RequestCommand> boolean deserializeContent(T request)
            throws DeserializationException;

    /**
     * Deserialize the content of ResponseCommand.
     *
     * @param response
     * @param invokeContext
     * @return
     * @throws CodecException
     */
    <T extends ResponseCommand> boolean deserializeContent(T response, InvokeContext invokeContext)
            throws DeserializationException;
}