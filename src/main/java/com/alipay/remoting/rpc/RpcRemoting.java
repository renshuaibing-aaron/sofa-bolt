package com.alipay.remoting.rpc;

import com.alipay.remoting.*;
import com.alipay.remoting.config.switches.ProtocolSwitch;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.exception.SerializationException;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.protocol.RpcProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcRequestCommand;
import com.alipay.remoting.util.RemotingUtil;
import org.slf4j.Logger;

/**
 * Rpc remoting capability.
 *远程调用执行抽象类 服务端和客户端都继承了这个类
 *   注意这个类的一个静态方法 会初始化两种协议
 * @author jiangping
 * @version $Id: RpcRemoting.java, v 0.1 Mar 6, 2016 9:09:48 PM tao Exp $
 */
public abstract class RpcRemoting extends BaseRemoting {
    /**
     * logger
     */
    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    static {
        System.out.println("创建两种协议实例 + 注册到 RpcProtocolManager");
        RpcProtocolManager.initProtocols();
    }

    /**
     * address parser to get custom args
     */
    protected RemotingAddressParser addressParser;

    /**
     * connection manager
     */
    protected DefaultConnectionManager connectionManager;

    /**
     * default constructor
     */
    public RpcRemoting(CommandFactory commandFactory) {
        super(commandFactory);
    }

    /**
     * @param addressParser
     * @param connectionManager
     */
    public RpcRemoting(CommandFactory commandFactory, RemotingAddressParser addressParser,
                       DefaultConnectionManager connectionManager) {
        this(commandFactory);
        this.addressParser = addressParser;
        this.connectionManager = connectionManager;
    }

    /**
     * Oneway rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void oneway(final String addr, final Object request, final InvokeContext invokeContext)
            throws RemotingException,
            InterruptedException {
        Url url = this.addressParser.parse(addr);
        this.oneway(url, request, invokeContext);
    }

    /**
     * Oneway rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param url
     * @param request
     * @param invokeContext
     * @throws RemotingException
     * @throws InterruptedException
     */
    public abstract void oneway(final Url url, final Object request,
                                final InvokeContext invokeContext) throws RemotingException,
            InterruptedException;

    /**
     * Oneway rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @throws RemotingException
     */
    public void oneway(final Connection conn, final Object request,
                       final InvokeContext invokeContext) throws RemotingException {
        RequestCommand requestCommand = (RequestCommand) toRemotingCommand(request, conn,
                invokeContext, -1);
        requestCommand.setType(RpcCommandType.REQUEST_ONEWAY);
        preProcessInvokeContext(invokeContext, requestCommand, conn);
        super.oneway(conn, requestCommand);
    }

    /**
     * Synchronous rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(final String addr, final Object request, final InvokeContext invokeContext, final int timeoutMillis)
            throws RemotingException,
            InterruptedException {
         //"127.0.0.1:8888", request,  null, 30 * 1000
        System.out.println("将 addr 转化为 Url");
        Url url = this.addressParser.parse(addr);
        return this.invokeSync(url, request, invokeContext, timeoutMillis);
    }

    /**
     * Synchronous rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param url
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public abstract Object invokeSync(final Url url, final Object request,
                                      final InvokeContext invokeContext, final int timeoutMillis)
            throws RemotingException,
            InterruptedException;

    /**
     * Synchronous rpc invocation.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public Object invokeSync(final Connection conn, final Object request, final InvokeContext invokeContext, final int timeoutMillis)
            throws RemotingException,InterruptedException
             {
        //创建请求对象  序列化
        RemotingCommand requestCommand = toRemotingCommand(request,conn,invokeContext,timeoutMillis);

        preProcessInvokeContext(invokeContext, requestCommand, conn);

        //发起请求
        ResponseCommand responseCommand = (ResponseCommand) super.invokeSync(conn, requestCommand, timeoutMillis);

        responseCommand.setInvokeContext(invokeContext);

        System.out.println("客户端 netty worker 线程接收响应并填充到指定 invokeId 的 InvokeFuture 中，唤醒如下流程");
        // 解析响应（反序列化）
        Object responseObject = RpcResponseResolver.resolveResponseObject(responseCommand,RemotingUtil.parseRemoteAddress(conn.getChannel()));


        return responseObject;
    }

    /**
     * Rpc invocation with future returned.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public RpcResponseFuture invokeWithFuture(final String addr, final Object request,
                                              final InvokeContext invokeContext, int timeoutMillis)
            throws RemotingException,
            InterruptedException {
        Url url = this.addressParser.parse(addr);
        return this.invokeWithFuture(url, request, invokeContext, timeoutMillis);
    }

    /**
     * Rpc invocation with future returned.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param url
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    public abstract RpcResponseFuture invokeWithFuture(final Url url, final Object request,
                                                       final InvokeContext invokeContext,
                                                       final int timeoutMillis)
            throws RemotingException,
            InterruptedException;

    /**
     * Rpc invocation with future returned.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     */
    public RpcResponseFuture invokeWithFuture(final Connection conn, final Object request,
                                              final InvokeContext invokeContext,
                                              final int timeoutMillis) throws RemotingException {

        RemotingCommand requestCommand = toRemotingCommand(request, conn, invokeContext,
                timeoutMillis);

        preProcessInvokeContext(invokeContext, requestCommand, conn);
        InvokeFuture future = super.invokeWithFuture(conn, requestCommand, timeoutMillis);
        return new RpcResponseFuture(RemotingUtil.parseRemoteAddress(conn.getChannel()), future);
    }

    /**
     * Rpc invocation with callback.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param addr
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    public void invokeWithCallback(String addr, Object request, final InvokeContext invokeContext,
                                   InvokeCallback invokeCallback, int timeoutMillis)
            throws RemotingException,
            InterruptedException {
        Url url = this.addressParser.parse(addr);
        this.invokeWithCallback(url, request, invokeContext, invokeCallback, timeoutMillis);
    }

    /**
     * Rpc invocation with callback.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param url
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    public abstract void invokeWithCallback(final Url url, final Object request,
                                            final InvokeContext invokeContext,
                                            final InvokeCallback invokeCallback,
                                            final int timeoutMillis) throws RemotingException,
            InterruptedException;

    /**
     * Rpc invocation with callback.<br>
     * Notice! DO NOT modify the request object concurrently when this method is called.
     *
     * @param conn
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     */
    public void invokeWithCallback(final Connection conn, final Object request,
                                   final InvokeContext invokeContext,
                                   final InvokeCallback invokeCallback, final int timeoutMillis)
            throws RemotingException {
        RemotingCommand requestCommand = toRemotingCommand(request, conn, invokeContext,
                timeoutMillis);
        preProcessInvokeContext(invokeContext, requestCommand, conn);
        super.invokeWithCallback(conn, requestCommand, invokeCallback, timeoutMillis);
    }

    /**
     * Convert application request object to remoting request command.
     *
     * @param request
     * @param conn
     * @param timeoutMillis
     * @return
     * @throws CodecException
     */
    protected RemotingCommand toRemotingCommand(Object request, Connection conn, InvokeContext invokeContext, int timeoutMillis)
            throws SerializationException {
        //创建请求对象
        RpcRequestCommand command = this.getCommandFactory().createRequestCommand(request);

        if (null != invokeContext) {
            // set client custom serializer for request command if not null
            //设置调用级别的 Serializer
            Object clientCustomSerializer = invokeContext.get(InvokeContext.BOLT_CUSTOM_SERIALIZER);
            if (null != clientCustomSerializer) {
                try {
                    command.setSerializer((Byte) clientCustomSerializer);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(
                            "Illegal custom serializer [" + clientCustomSerializer
                                    + "], the type of value should be [byte], but now is ["
                                    + clientCustomSerializer.getClass().getName() + "].");
                }
            }

            // enable crc by default, user can disable by set invoke context `false` for key `InvokeContext.BOLT_CRC_SWITCH`
            Boolean crcSwitch = invokeContext.get(InvokeContext.BOLT_CRC_SWITCH,
                    ProtocolSwitch.CRC_SWITCH_DEFAULT_VALUE);
            if (null != crcSwitch && crcSwitch) {
                command.setProtocolSwitch(ProtocolSwitch
                        .create(new int[]{ProtocolSwitch.CRC_SWITCH_INDEX}));
            }
        } else {
            // enable crc by default, if there is no invoke context.
            command.setProtocolSwitch(ProtocolSwitch
                    .create(new int[]{ProtocolSwitch.CRC_SWITCH_INDEX}));
        }

        command.setTimeout(timeoutMillis);
        command.setRequestClass(request.getClass().getName());
        command.setInvokeContext(invokeContext);

        //序列化方法(这里是干嘛的)
        command.serialize();

        logDebugInfo(command);
        return command;
    }

    protected abstract void preProcessInvokeContext(InvokeContext invokeContext,
                                                    RemotingCommand cmd, Connection connection);

    /**
     * @param requestCommand
     */
    private void logDebugInfo(RemotingCommand requestCommand) {
        if (logger.isDebugEnabled()) {
            logger.debug("Send request, requestId=" + requestCommand.getId());
        }
    }

    /**
     * @see com.alipay.remoting.BaseRemoting#createInvokeFuture(com.alipay.remoting.RemotingCommand, com.alipay.remoting.InvokeContext)
     */
    @Override
    protected InvokeFuture createInvokeFuture(RemotingCommand request, InvokeContext invokeContext) {
        return new DefaultInvokeFuture(request.getId(), null, null, request.getProtocolCode().getFirstByte(), this.getCommandFactory(), invokeContext);
    }

    /**
     * @see com.alipay.remoting.BaseRemoting#createInvokeFuture(Connection, RemotingCommand, InvokeContext, InvokeCallback)
     */
    @Override
    protected InvokeFuture createInvokeFuture(Connection conn, RemotingCommand request,
                                              InvokeContext invokeContext,
                                              InvokeCallback invokeCallback) {
        return new DefaultInvokeFuture(request.getId(), new RpcInvokeCallbackListener(
                RemotingUtil.parseRemoteAddress(conn.getChannel())), invokeCallback, request
                .getProtocolCode().getFirstByte(), this.getCommandFactory(), invokeContext);
    }
}
