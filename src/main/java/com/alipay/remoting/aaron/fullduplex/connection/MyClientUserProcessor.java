package com.alipay.remoting.aaron.fullduplex.connection;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.aaron.bean.MyRequest;
import com.alipay.remoting.aaron.bean.MyResponse;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;

public class MyClientUserProcessor extends SyncUserProcessor<MyRequest> {
    @Override
    public Object handleRequest(BizContext bizCtx, MyRequest request) throws Exception {
        MyResponse response = new MyResponse();
        if (request != null) {
            System.out.println(request);
            response.setResp("from client -> " + request.getReq());
        }
        return response;
    }

    @Override
    public String interest() {
        return MyRequest.class.getName();
    }
}