package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.*;
import com.alipay.remoting.rpc.RpcCommandFactory;

/**
 *
 * 请求命令
 * Request command protocol for v1
 * 0     1     2           4           6           8          10           12          14         16
 * +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
 * |proto| type| cmdcode   |ver2 |   requestId           |codec|        timeout        |  classLen |
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * |headerLen  | contentLen            |                             ... ...                       |
 * +-----------+-----------+-----------+                                                                                               +
 * |               className + header  + content  bytes                                            |
 * +                                                                                               +
 * |                               ... ...                                                         |
 * +-----------------------------------------------------------------------------------------------+
 * <p>
 * proto: code for protocol
 * type: request/response/request oneway
 * cmdcode: code for remoting command
 * ver2:version for remoting command
 * requestId: id of request
 * codec: code for codec
 * headerLen: length of header
 * contentLen: length of content
 * <p>
 *  响应命令
 * Response command protocol for v1
 * 0     1     2     3     4           6           8          10           12          14         16
 * +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
 * |proto| type| cmdcode   |ver2 |   requestId           |codec|respstatus |  classLen |headerLen  |
 * +-----------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+
 * | contentLen            |                  ... ...                                              |
 * +-----------------------+                                                                       +
 * |                         className + header  + content  bytes                                  |
 * +                                                                                               +
 * |                               ... ...                                                         |
 * +-----------------------------------------------------------------------------------------------+
 * respstatus: response status
 *
 * @author jiangping
 * @version $Id: RpcProtocol.java, v 0.1 2015-9-28 PM7:04:04 tao Exp $
 */
public class RpcProtocol implements Protocol {

    /**
     * 协议的标识 用来查找编解码器
     */
    public static final byte PROTOCOL_CODE = (byte) 1;
    private static final int REQUEST_HEADER_LEN = 22;
    private static final int RESPONSE_HEADER_LEN = 20;

    private CommandEncoder encoder;
    private CommandDecoder decoder;

    private HeartbeatTrigger heartbeatTrigger;
    private CommandHandler commandHandler;
    private CommandFactory commandFactory;

    public RpcProtocol() {
        //最底层编码器
        this.encoder = new RpcCommandEncoder();

        //最底层解码器
        this.decoder = new RpcCommandDecoder();
        //<!-- 3.2.2 创建请求信息和返回信息包装体的工厂 -->
        this.commandFactory = new RpcCommandFactory();

        //最底层的连接心跳处理器
        this.heartbeatTrigger = new RpcHeartbeatTrigger(this.commandFactory);

        //命令处理器
        this.commandHandler = new RpcCommandHandler(this.commandFactory);
    }

    /**
     * Get the length of request header.
     */
    public static int getRequestHeaderLength() {
        return RpcProtocol.REQUEST_HEADER_LEN;
    }

    /**
     * Get the length of response header.
     */
    public static int getResponseHeaderLength() {
        return RpcProtocol.RESPONSE_HEADER_LEN;
    }

    @Override
    public CommandEncoder getEncoder() {
        return this.encoder;
    }

    @Override
    public CommandDecoder getDecoder() {
        return this.decoder;
    }

    @Override
    public HeartbeatTrigger getHeartbeatTrigger() {
        return this.heartbeatTrigger;
    }

    @Override
    public CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return this.commandFactory;
    }
}
