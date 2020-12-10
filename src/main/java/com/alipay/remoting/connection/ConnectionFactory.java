
package com.alipay.remoting.connection;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventHandler;
import com.alipay.remoting.Url;

/**
 * Factory that creates connections.
 * ConnectionFactory 连接工厂：创建连接、检测连接等  分析一个实现类AbstractConnectionFactory
 * @author jiangping
 * @version $Id: ConnectionFactory.java, v 0.1 2015-9-21 PM7:47:46 tao Exp $
 */
public interface ConnectionFactory {

    /**
     * Initialize the factory.
     */
    void init(ConnectionEventHandler connectionEventHandler);

    /**
     * Create a connection use #BoltUrl
     *
     * @param url target url
     * @return connection
     */
    Connection createConnection(Url url) throws Exception;

    /**
     * Create a connection according to the IP and port.
     * Note: The default protocol is RpcProtocol.
     *
     * @param targetIP       target ip
     * @param targetPort     target port
     * @param connectTimeout connect timeout in millisecond
     * @return connection
     */
    Connection createConnection(String targetIP, int targetPort, int connectTimeout)
            throws Exception;

    /**
     * Create a connection according to the IP and port.
     * <p>
     * Note: The default protocol is RpcProtocolV2, and you can specify the version
     *
     * @param targetIP       target ip
     * @param targetPort     target port
     * @param version        protocol version
     * @param connectTimeout connect timeout in millisecond
     * @return connection
     */
    Connection createConnection(String targetIP, int targetPort, byte version, int connectTimeout)
            throws Exception;
}
