package com.alipay.remoting.rpc;

import com.alipay.remoting.*;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.util.RemotingUtil;

/**
 * Rpc client remoting
 *客户端远程调用执行类
 * @author xiaomin.cxm
 * @version $Id: RpcClientRemoting.java, v 0.1 Apr 14, 2016 11:58:56 AM xiaomin.cxm Exp $
 */
public class RpcClientRemoting extends RpcRemoting {

    public RpcClientRemoting(CommandFactory commandFactory, RemotingAddressParser addressParser,
                             DefaultConnectionManager connectionManager) {
        super(commandFactory, addressParser, connectionManager);
    }

    /**
     * @see com.alipay.remoting.rpc.RpcRemoting#oneway(com.alipay.remoting.Url, java.lang.Object, InvokeContext)
     */
    @Override
    public void oneway(Url url, Object request, InvokeContext invokeContext) throws RemotingException,InterruptedException {
        // 获取或者创建 Connection
        final Connection conn = getConnectionAndInitInvokeContext(url, invokeContext);
        // 检测连接
        this.connectionManager.check(conn);
        this.oneway(conn, request, invokeContext);
    }

    /**
     * @see com.alipay.remoting.rpc.RpcRemoting#invokeSync(com.alipay.remoting.Url, java.lang.Object, InvokeContext, int)
     * //"127.0.0.1:8888", request,  null, 30 * 1000
     */
    @Override
    public Object invokeSync(Url url, Object request, InvokeContext invokeContext, int timeoutMillis)
            throws RemotingException,
            InterruptedException {

        //获取连接
        final Connection conn = getConnectionAndInitInvokeContext(url, invokeContext);

        // 校验 connection 不为 null && channel 不为 null && channel 是 active 状态 && channel 可写
        this.connectionManager.check(conn);
        return this.invokeSync(conn, request, invokeContext, timeoutMillis);
    }

    /**
     * @see com.alipay.remoting.rpc.RpcRemoting#invokeWithFuture(com.alipay.remoting.Url, java.lang.Object, InvokeContext, int)
     */
    @Override
    public RpcResponseFuture invokeWithFuture(Url url, Object request, InvokeContext invokeContext,
                                              int timeoutMillis) throws RemotingException,
            InterruptedException {
        final Connection conn = getConnectionAndInitInvokeContext(url, invokeContext);
        this.connectionManager.check(conn);
        return this.invokeWithFuture(conn, request, invokeContext, timeoutMillis);
    }

    /**
     * @see com.alipay.remoting.rpc.RpcRemoting#invokeWithCallback(com.alipay.remoting.Url, java.lang.Object, InvokeContext, com.alipay.remoting.InvokeCallback, int)
     */
    @Override
    public void invokeWithCallback(Url url, Object request, InvokeContext invokeContext,
                                   InvokeCallback invokeCallback, int timeoutMillis)
            throws RemotingException,
            InterruptedException {
        final Connection conn = getConnectionAndInitInvokeContext(url, invokeContext);
        this.connectionManager.check(conn);
        this.invokeWithCallback(conn, request, invokeContext, invokeCallback, timeoutMillis);
    }

    /**
     * @see RpcRemoting#preProcessInvokeContext(InvokeContext, RemotingCommand, Connection)
     */
    @Override
    protected void preProcessInvokeContext(InvokeContext invokeContext, RemotingCommand cmd,
                                           Connection connection) {
        if (null != invokeContext) {
            invokeContext.putIfAbsent(InvokeContext.CLIENT_LOCAL_IP,
                    RemotingUtil.parseLocalIP(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.CLIENT_LOCAL_PORT,
                    RemotingUtil.parseLocalPort(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.CLIENT_REMOTE_IP,
                    RemotingUtil.parseRemoteIP(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.CLIENT_REMOTE_PORT,
                    RemotingUtil.parseRemotePort(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.BOLT_INVOKE_REQUEST_ID, cmd.getId());
        }
    }

    /**
     * Get connection and set init invokeContext if invokeContext not {@code null}
     *
     * @param url           target url
     * @param invokeContext invoke context to set
     * @return connection
     */
    protected Connection getConnectionAndInitInvokeContext(Url url, InvokeContext invokeContext)
            throws RemotingException,
            InterruptedException {
        long start = System.currentTimeMillis();
        Connection conn;
        try {
            //DefaultConnectionManager.getAndCreateIfAbsent(Url url)
            conn = this.connectionManager.getAndCreateIfAbsent(url);

        } finally {
            // 记录连接获取或者创建的时间消耗
            if (null != invokeContext) {
                invokeContext.putIfAbsent(InvokeContext.CLIENT_CONN_CREATETIME,
                        (System.currentTimeMillis() - start));
            }
        }
        return conn;
    }
}
