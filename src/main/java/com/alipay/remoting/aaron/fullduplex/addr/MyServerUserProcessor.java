package com.alipay.remoting.aaron.fullduplex.addr;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.aaron.bean.MyRequest;
import com.alipay.remoting.aaron.bean.MyResponse;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;

public class MyServerUserProcessor extends SyncUserProcessor<MyRequest> {
    // 存储 client 端地址，用于发起远程调用
    private String remoteAddr;

    @Override
    public Object handleRequest(BizContext bizCtx, MyRequest request) throws Exception {
        remoteAddr = bizCtx.getRemoteAddress(); // 此处也可以存储 Connection：bizCtx.getConnection();
        MyResponse response = new MyResponse();
        if (request != null) {
            System.out.println(request);
            response.setResp("from server -> " + request.getReq());
        }

        return response;
    }

    @Override
    public String interest() {
        return MyRequest.class.getName();
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }
}