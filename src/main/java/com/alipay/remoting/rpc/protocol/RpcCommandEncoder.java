package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.CommandEncoder;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.rpc.RpcCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

import java.io.Serializable;

/**
 * Encode remoting command into ByteBuf.
 *   编码真实类
 * @author jiangping
 * @version $Id: RpcCommandEncoder.java, v 0.1 2015-8-31 PM8:11:27 tao Exp $
 */
public class RpcCommandEncoder implements CommandEncoder {
    /**
     * logger
     */
    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    /**
     * 当发起请求时，例如 invokeSync() 时，RpcRemoting 会先对请求数据进行序列化，之后编码发送
     * @see com.alipay.remoting.CommandEncoder#encode(io.netty.channel.ChannelHandlerContext, java.io.Serializable, io.netty.buffer.ByteBuf)
     */
    @Override
    public void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        System.out.println("========RpcCommandEncoder.encode=============");
        try {
            if (msg instanceof RpcCommand) {
                /*
                 * ver: version for protocol
                 * type: request/response/request oneway
                 * cmdcode: code for remoting command
                 * ver2:version for remoting command
                 * requestId: id of request
                 * codec: code for codec
                 * (req)timeout: request timeout.
                 * (resp)respStatus: response status
                 * classLen: length of request or response class name
                 * headerLen: length of header
                 * cotentLen: length of content
                 * className
                 * header
                 * content
                 */
                RpcCommand cmd = (RpcCommand) msg;
                out.writeByte(RpcProtocol.PROTOCOL_CODE);
                out.writeByte(cmd.getType());
                out.writeShort(((RpcCommand) msg).getCmdCode().value());
                out.writeByte(cmd.getVersion());
                out.writeInt(cmd.getId());
                out.writeByte(cmd.getSerializer());

                if (cmd instanceof RequestCommand) {
                    //timeout
                    out.writeInt(((RequestCommand) cmd).getTimeout());
                }
                if (cmd instanceof ResponseCommand) {
                    //response status
                    ResponseCommand response = (ResponseCommand) cmd;
                    out.writeShort(response.getResponseStatus().getValue());
                }
                out.writeShort(cmd.getClazzLength());
                out.writeShort(cmd.getHeaderLength());
                out.writeInt(cmd.getContentLength());

                //这里有个疑问 为什么不在这里进行序列化，而在之前就进行序列化  岂不是增加网络传输的带宽

                //妈的 这里的命名也不规范
                if (cmd.getClazzLength() > 0) {
                    out.writeBytes(cmd.getClazz());
                }
                if (cmd.getHeaderLength() > 0) {
                    out.writeBytes(cmd.getHeader());
                }
                if (cmd.getContentLength() > 0) {
                    out.writeBytes(cmd.getContent());
                }
            } else {
                String warnMsg = "msg type [" + msg.getClass() + "] is not subclass of RpcCommand";
                logger.warn(warnMsg);
            }
        } catch (Exception e) {
            logger.error("Exception caught!", e);
            throw e;
        }
    }
}
