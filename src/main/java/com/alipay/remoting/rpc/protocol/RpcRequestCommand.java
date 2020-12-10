package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.CustomSerializer;
import com.alipay.remoting.CustomSerializerManager;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.config.Configs;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.serialization.Serializer;
import com.alipay.remoting.serialization.SerializerManager;
import com.alipay.remoting.util.IDGenerator;

import java.io.UnsupportedEncodingException;

/**
 * Request command for Rpc.
 *RpcRequestCommand 存储了真实的业务数据（clazzName、header、content），
 * 并提供了 customSerializer 对象（该对象通过 CustomSerializerManager 进行获取），并提供了三种类型业务数据的序列化和反序列化实现
 * @author jiangping
 * @version $Id: RpcRequestCommand.java, v 0.1 2015-9-25 PM2:13:35 tao Exp $
 */
public class RpcRequestCommand extends RequestCommand {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -4602613826188210946L;
    private Object requestObject;
    private String requestClass;

    private CustomSerializer customSerializer;
    private Object requestHeader;

    private transient long arriveTime = -1;

    /**
     * create request command without id
     */
    public RpcRequestCommand() {
        super(RpcCommandCode.RPC_REQUEST);
    }

    /**
     * create request command with id and request object
     *
     * @param request request object
     */
    public RpcRequestCommand(Object request) {
        // 设置唯一id + 消息类型为 Request（还有 Response 和 heartbeat）+ MyRequest request
        super(RpcCommandCode.RPC_REQUEST);
        this.requestObject = request;
        //
        this.setId(IDGenerator.nextId());
    }

    @Override
    public void serializeClazz() throws SerializationException {
        if (this.requestClass != null) {
            try {
                byte[] clz = this.requestClass.getBytes(Configs.DEFAULT_CHARSET);
                this.setClazz(clz);
            } catch (UnsupportedEncodingException e) {
                throw new SerializationException("Unsupported charset: " + Configs.DEFAULT_CHARSET,
                        e);
            }
        }
    }

    @Override
    public void deserializeClazz() throws DeserializationException {
        if (this.getClazz() != null && this.getRequestClass() == null) {
            try {
                this.setRequestClass(new String(this.getClazz(), Configs.DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                throw new DeserializationException("Unsupported charset: "
                        + Configs.DEFAULT_CHARSET, e);
            }
        }
    }

    @Override
    public void serializeHeader(InvokeContext invokeContext) throws SerializationException {
        if (this.getCustomSerializer() != null) {
            try {
                this.getCustomSerializer().serializeHeader(this, invokeContext);
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException(
                        "Exception caught when serialize header of rpc request command!", e);
            }
        }
    }

    @Override
    public void deserializeHeader(InvokeContext invokeContext) throws DeserializationException {
        if (this.getHeader() != null && this.getRequestHeader() == null) {
            if (this.getCustomSerializer() != null) {
                try {
                    this.getCustomSerializer().deserializeHeader(this);
                } catch (DeserializationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DeserializationException(
                            "Exception caught when deserialize header of rpc request command!", e);
                }
            }
        }
    }

    @Override
    public void serializeContent(InvokeContext invokeContext) throws SerializationException {
        if (this.requestObject != null) {
            try {
                if (this.getCustomSerializer() != null
                        && this.getCustomSerializer().serializeContent(this, invokeContext)) {
                    return;
                }

                this.setContent(SerializerManager.getSerializer(this.getSerializer()).serialize(
                        this.requestObject));
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException(
                        "Exception caught when serialize content of rpc request command!", e);
            }
        }
    }

    //这里说明 在序列化和反序列化的过程中 首先使用用户自定义的序列化器 为空使用bolt默认的序列化器
    @Override
    public void deserializeContent(InvokeContext invokeContext) throws DeserializationException {
        if (this.getRequestObject() == null) {
            try {
                if (this.getCustomSerializer() != null && this.getCustomSerializer().deserializeContent(this)) {
                    return;
                }
                if (this.getContent() != null) {
                    Serializer serializer = SerializerManager.getSerializer(this.getSerializer());
                    this.setRequestObject(serializer.deserialize(this.getContent(), this.requestClass));
                }
            } catch (DeserializationException e) {
                throw e;
            } catch (Exception e) {
                throw new DeserializationException(
                        "Exception caught when deserialize content of rpc request command!", e);
            }
        }
    }

    /**
     * Getter method for property <tt>requestObject</tt>.
     *
     * @return property value of requestObject
     */
    public Object getRequestObject() {
        return requestObject;
    }

    /**
     * Setter method for property <tt>requestObject</tt>.
     *
     * @param requestObject value to be assigned to property requestObject
     */
    public void setRequestObject(Object requestObject) {
        this.requestObject = requestObject;
    }

    /**
     * Getter method for property <tt>requestHeader</tt>.
     *
     * @return property value of requestHeader
     */
    public Object getRequestHeader() {
        return requestHeader;
    }

    /**
     * Setter method for property <tt>requestHeader</tt>.
     *
     * @param requestHeader value to be assigned to property requestHeader
     */
    public void setRequestHeader(Object requestHeader) {
        this.requestHeader = requestHeader;
    }

    /**
     * Getter method for property <tt>requestClass</tt>.
     *
     * @return property value of requestClass
     */
    public String getRequestClass() {
        return requestClass;
    }

    /**
     * Setter method for property <tt>requestClass</tt>.
     *
     * @param requestClass value to be assigned to property requestClass
     */
    public void setRequestClass(String requestClass) {
        this.requestClass = requestClass;
    }

    /**
     * Getter method for property <tt>customSerializer</tt>.
     *
     * @return property value of customSerializer
     */
    public CustomSerializer getCustomSerializer() {
        if (this.customSerializer != null) {
            return customSerializer;
        }
        if (this.requestClass != null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.requestClass);
        }
        if (this.customSerializer == null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.getCmdCode());
        }
        return this.customSerializer;
    }

    /**
     * Getter method for property <tt>arriveTime</tt>.
     *
     * @return property value of arriveTime
     */
    public long getArriveTime() {
        return arriveTime;
    }

    /**
     * Setter method for property <tt>arriveTime</tt>.
     *
     * @param arriveTime value to be assigned to property arriveTime
     */
    public void setArriveTime(long arriveTime) {
        this.arriveTime = arriveTime;
    }
}
