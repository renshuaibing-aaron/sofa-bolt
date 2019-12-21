package com.alipay.remoting.aaron.fullduplex.connection;

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.aaron.bean.MyRequest;
import com.alipay.remoting.aaron.bean.MyResponse;
import com.alipay.remoting.aaron.fullduplex.addr.MyServerUserProcessor;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcServer;

public class MyServer {
    public static void main(String[] args) throws RemotingException, InterruptedException {

        RpcServer server= new RpcServer(8888);

        MyServerUserProcessor serverUserProcessor = new MyServerUserProcessor();

        server.registerUserProcessor(serverUserProcessor);

        // 创建并注册 ConnectionEventType.CONNECT 连接事件处理器
        MyConnectEventProcessor connectEventProcessor = new MyConnectEventProcessor();
        server.addConnectionEventProcessor(ConnectionEventType.CONNECT, connectEventProcessor);


        if (server.start()) {

            System.out.println("server start success!");
            // 模拟去其他事情
            Thread.sleep(10000);
            System.out.println("======服务器主动向客户端发消息=========="+connectEventProcessor.getConnection());
            MyRequest request = new MyRequest();
            request.setReq("hi, bolt-client,我是服务端");


            // 向 connectEventProcessor 存储的 connection 发起请求
            MyResponse resp = (MyResponse)server.invokeSync(connectEventProcessor.getConnection(), request, 10000);

            System.out.println(resp.getResp());
        } else {
            System.out.println("server start fail!");
        }
    }
}