package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.CommandCode;
import com.alipay.remoting.CommandDecoder;
import com.alipay.remoting.ResponseStatus;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Command decoder for Rpc.
 *解码真实类
 * @author jiangping
 * @version $Id: RpcCommandDecoder.java, v 0.1 2015-10-14 PM5:15:26 tao Exp $
 */
public class RpcCommandDecoder implements CommandDecoder {

    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    // 获取 RpcProtocol 协议下的响应头（20）和请求头（22）的长度，即为 20
    private int lessLen;

    {
        lessLen = RpcProtocol.getResponseHeaderLength() < RpcProtocol.getRequestHeaderLength() ? RpcProtocol
                .getResponseHeaderLength() : RpcProtocol.getRequestHeaderLength();
    }

    /**
     * @see com.alipay.remoting.CommandDecoder#decode(io.netty.channel.ChannelHandlerContext, io.netty.buffer.ByteBuf, java.util.List)
     */
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("======RpcCommandDecoder.decode==2222==============");
        // the less length between response header and request header
        if (in.readableBytes() >= lessLen) {

            // 标记当前读指针的位置
            in.markReaderIndex();
            // 读取 protocolCode
            byte protocol = in.readByte();

            // 恢复读指针到读取 protocolCode 之前
            in.resetReaderIndex();
            if (protocol == RpcProtocol.PROTOCOL_CODE) {
                /*
                 * ver: version for protocol
                 * type: request/response/request oneway
                 * cmdcode: code for remoting command
                 * ver2:version for remoting command
                 * requestId: id of request
                 * codec: code for codec
                 * (req)timeout: request timeout
                 * (resp)respStatus: response status
                 * classLen: length of request or response class name
                 * headerLen: length of header
                 * contentLen: length of content
                 * className
                 * header
                 * content
                 */
                if (in.readableBytes() > 2) {
                    // 读取 protocol_code
                    in.markReaderIndex();
                    // 读取 type
                    in.readByte(); //version
                    byte type = in.readByte(); //type

                    // 解码请求
                    if (type == RpcCommandType.REQUEST || type == RpcCommandType.REQUEST_ONEWAY) {
                        //decode request
                        if (in.readableBytes() >= RpcProtocol.getRequestHeaderLength() - 2) {

                            // CommandCode ：请求命令类型，request / response / heartbeat
                            short cmdCode = in.readShort();

                            // CommandVersion
                            byte ver2 = in.readByte();

                            // 请求ID
                            int requestId = in.readInt();

                            // 序列化器
                            byte serializer = in.readByte();
                            int timeout = in.readInt();
                            short classLen = in.readShort();
                            short headerLen = in.readShort();
                            int contentLen = in.readInt();
                            byte[] clazz = null;
                            byte[] header = null;
                            byte[] content = null;
                            if (in.readableBytes() >= classLen + headerLen + contentLen) {
                                if (classLen > 0) {
                                    clazz = new byte[classLen];
                                    in.readBytes(clazz);
                                }
                                if (headerLen > 0) {
                                    header = new byte[headerLen];
                                    in.readBytes(header);
                                }
                                // 解码内容，注意此时解码出来的都是一个 byte[]，需要序列化才能转化为真正对象
                                if (contentLen > 0) {
                                    content = new byte[contentLen];
                                    in.readBytes(content);
                                }
                            } else {// not enough data
                                // 不够一个完整包，返回，等待累加器类架构足够的内容
                                in.resetReaderIndex();
                                return;
                            }
                            RequestCommand command;
                            if (cmdCode == CommandCode.HEARTBEAT_VALUE) {

                                // 如果是心跳请求消息，直接创建一个 HeartbeatCommand
                                command = new HeartbeatCommand();
                            } else {

                                // 如果是正常请求，创建一个 RpcRequestCommand
                                command = createRequestCommand(cmdCode);
                            }
                            command.setType(type);
                            command.setVersion(ver2);
                            command.setId(requestId);
                            command.setSerializer(serializer);
                            command.setTimeout(timeout);
                            command.setClazz(clazz);
                            command.setHeader(header);
                            command.setContent(content);
                            out.add(command);

                        } else {
                            in.resetReaderIndex();
                        }

                        // 解码响应
                    } else if (type == RpcCommandType.RESPONSE) {
                        //decode response
                        if (in.readableBytes() >= RpcProtocol.getResponseHeaderLength() - 2) {
                            short cmdCode = in.readShort();
                            byte ver2 = in.readByte();
                            int requestId = in.readInt();
                            byte serializer = in.readByte();
                            short status = in.readShort();
                            short classLen = in.readShort();
                            short headerLen = in.readShort();
                            int contentLen = in.readInt();
                            byte[] clazz = null;
                            byte[] header = null;
                            byte[] content = null;
                            if (in.readableBytes() >= classLen + headerLen + contentLen) {
                                if (classLen > 0) {
                                    clazz = new byte[classLen];
                                    in.readBytes(clazz);
                                }
                                if (headerLen > 0) {
                                    header = new byte[headerLen];
                                    in.readBytes(header);
                                }
                                if (contentLen > 0) {
                                    content = new byte[contentLen];
                                    in.readBytes(content);
                                }
                            } else {// not enough data
                                in.resetReaderIndex();
                                return;
                            }
                            ResponseCommand command;
                            if (cmdCode == CommandCode.HEARTBEAT_VALUE) {

                                command = new HeartbeatAckCommand();
                            } else {
                                command = createResponseCommand(cmdCode);
                            }
                            command.setType(type);
                            command.setVersion(ver2);
                            command.setId(requestId);
                            command.setSerializer(serializer);
                            command.setResponseStatus(ResponseStatus.valueOf(status));
                            command.setClazz(clazz);
                            command.setHeader(header);
                            command.setContent(content);
                            command.setResponseTimeMillis(System.currentTimeMillis());
                            command.setResponseHost((InetSocketAddress) ctx.channel()
                                    .remoteAddress());
                            out.add(command);
                        } else {
                            in.resetReaderIndex();
                        }
                    } else {
                        String emsg = "Unknown command type: " + type;
                        logger.error(emsg);
                        throw new RuntimeException(emsg);
                    }
                }

            } else {
                String emsg = "Unknown protocol: " + protocol;
                logger.error(emsg);
                throw new RuntimeException(emsg);
            }

        }
    }

    private ResponseCommand createResponseCommand(short cmdCode) {
        ResponseCommand command = new RpcResponseCommand();
        command.setCmdCode(RpcCommandCode.valueOf(cmdCode));
        return command;
    }

    private RpcRequestCommand createRequestCommand(short cmdCode) {
        RpcRequestCommand command = new RpcRequestCommand();
        command.setCmdCode(RpcCommandCode.valueOf(cmdCode));
        command.setArriveTime(System.currentTimeMillis());
        return command;
    }

}
