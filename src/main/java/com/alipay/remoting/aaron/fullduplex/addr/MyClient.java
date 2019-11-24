package com.alipay.remoting.aaron.fullduplex.addr;

import com.alipay.remoting.aaron.bean.MyRequest;
import com.alipay.remoting.aaron.bean.MyResponse;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;

import java.util.concurrent.CountDownLatch;

public class MyClient {
    private static RpcClient client;
    private static CountDownLatch latch = new CountDownLatch(1);

    public static void start() {
        client = new RpcClient();
        // 注册业务逻辑处理器
        client.registerUserProcessor(new MyClientUserProcessor());
        client.init();
    }

    public static void main(String[] args) throws RemotingException, InterruptedException {
        MyClient.start();
        MyRequest request = new MyRequest();
        request.setReq("hello, bolt-server");
        MyResponse response = (MyResponse) client.invokeSync("127.0.0.1:8888", request, 300 * 1000);
        System.out.println(response);
        latch.await();
    }
}
