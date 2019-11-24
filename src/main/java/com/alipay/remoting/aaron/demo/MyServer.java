package com.alipay.remoting.aaron.demo;

import com.alipay.remoting.rpc.RpcServer;

/**
 * 服务端
 */
public class MyServer {
    public static boolean start() {
        //创建 RpcServer 实例，指定监听 port: 这个server类里会生成编解码生成器工厂;监听器;处理器
        RpcServer server = new RpcServer(8888);

        //注册业务逻辑处理器 UserProcessor
        server.registerUserProcessor(new MyServerUserProcessor());

        // 启动服务端：先做 netty 配置初始化操作，再做 bind 操作
        // 配置 netty 参数两种方式：[SOFABolt 源码分析11 - Config 配置管理的设计](https://www.jianshu.com/p/76b0be893745)
        return server.start();
    }

    public static void main(String[] args) {
        if (MyServer.start()) {
            System.out.println("server start success!");
        } else {
            System.out.println("server start fail!");
        }
    }
}