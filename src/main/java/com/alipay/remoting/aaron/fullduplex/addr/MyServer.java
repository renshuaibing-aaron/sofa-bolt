package com.alipay.remoting.aaron.fullduplex.addr;

import com.alipay.remoting.aaron.bean.MyRequest;
import com.alipay.remoting.aaron.bean.MyResponse;
import com.alipay.remoting.config.switches.GlobalSwitch;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcServer;

public class MyServer {
    public static void main(String[] args) throws RemotingException, InterruptedException {
        RpcServer server= new RpcServer(8888);
        MyServerUserProcessor serverUserProcessor = new MyServerUserProcessor();
        server.registerUserProcessor(serverUserProcessor);
        // 打开服务端连接管理功能
        server.switches().turnOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH);

        if (server.start()) {
            System.out.println("server start success!");
            // 模拟去其他事情
            Thread.sleep(50000);
            System.out.println("======服务器主动向客户端发消息=========="+serverUserProcessor.getRemoteAddr());
            MyRequest request = new MyRequest();
            request.setReq("hi, bolt-client，我是服务端");
            // 向 serverUserProcessor 存储的 RemoteAddr 发起请求
            MyResponse resp = (MyResponse)server.invokeSync(serverUserProcessor.getRemoteAddr(), request, 10000);
            System.out.println(resp.getResp());
        } else {
            System.out.println("server start fail!");
        }
    }
}
