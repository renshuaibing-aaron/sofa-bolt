package com.alipay.remoting;

import com.alipay.remoting.config.switches.ProtocolSwitch;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;

import java.io.Serializable;

/**
 * Remoting command.
 *命令接口
 * SOFABolt 对于请求和响应（实际上心跳也是）都会封装为 RemotingCommand 的实现类，然后在网络层进行传输。
 * 命令结构的设计
 * @author jiangping
 * @version $Id: RemotingCommand.java, v 0.1 2015-12-11 PM10:17:11 tao Exp $
 */
public interface RemotingCommand extends Serializable {
    /**
     * Get the code of the protocol that this command belongs to
     *
     * @return protocol code
     */
    ProtocolCode getProtocolCode();

    /**
     * Get the command code for this command
     *
     * @return command code
     */
    CommandCode getCmdCode();

    /**
     * Get the id of the command
     *
     * @return an int value represent the command id
     */
    int getId();

    /**
     * Get invoke context for this command
     *
     * @return context
     */
    InvokeContext getInvokeContext();

    /**
     * Get serializer type for this command
     *
     * @return
     */
    byte getSerializer();

    /**
     * Get the protocol switch status for this command
     *
     * @return
     */
    ProtocolSwitch getProtocolSwitch();

    /**
     * Serialize all parts of remoting command
     *
     * @throws SerializationException
     */
    void serialize() throws SerializationException;

    /**
     * Deserialize all parts of remoting command
     *
     * @throws DeserializationException
     */
    void deserialize() throws DeserializationException;

    /**
     * Serialize content of remoting command
     *
     * @param invokeContext
     * @throws SerializationException
     */
    void serializeContent(InvokeContext invokeContext) throws SerializationException;

    /**
     * Deserialize content of remoting command
     *
     * @param invokeContext
     * @throws DeserializationException
     */
    void deserializeContent(InvokeContext invokeContext) throws DeserializationException;

    ;
}
