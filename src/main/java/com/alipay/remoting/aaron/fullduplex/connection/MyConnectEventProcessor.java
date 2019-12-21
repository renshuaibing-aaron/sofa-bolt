package com.alipay.remoting.aaron.fullduplex.connection;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;

public class MyConnectEventProcessor implements ConnectionEventProcessor {
    // 存储连接，用于服务端向客户端发起远程通信
    private Connection connection;

    @Override
    public void onEvent(String remoteAddr, Connection conn) {
        this.connection = conn;
        System.out.println("hello, " + remoteAddr);
    }

    public Connection getConnection() {
        return connection;
    }
}