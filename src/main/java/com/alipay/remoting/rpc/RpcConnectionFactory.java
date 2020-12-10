package com.alipay.remoting.rpc;

import com.alipay.remoting.config.ConfigurableInstance;
import com.alipay.remoting.connection.ConnectionFactory;
import com.alipay.remoting.connection.DefaultConnectionFactory;
import com.alipay.remoting.rpc.protocol.UserProcessor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Default RPC connection factory impl.
 *
 * @author chengyi (mark.lx@antfin.com) 2018-06-20 15:32
 */
public class RpcConnectionFactory extends DefaultConnectionFactory implements ConnectionFactory {

    public RpcConnectionFactory(ConcurrentHashMap<String, UserProcessor<?>> userProcessors,
                                ConfigurableInstance configInstance) {
        // 创建 RpcCodec 编解码器工厂类
        // 创建心跳处理器
        // 创建业务逻辑处理器

        super(new RpcCodec(), new HeartbeatHandler(), new RpcHandler(userProcessors),
                configInstance);
        System.out.println("--RpcConnectionFactory--");
    }
}