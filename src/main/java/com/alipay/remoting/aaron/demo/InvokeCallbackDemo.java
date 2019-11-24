package com.alipay.remoting.aaron.demo;

import com.alipay.remoting.InvokeCallback;
import com.alipay.remoting.aaron.bean.MyResponse;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InvokeCallbackDemo implements InvokeCallback {
    @Override
    public void onResponse(Object result) {
        System.out.println("回调成功：" + (MyResponse) result);
    }

    @Override
    public void onException(Throwable e) {
        System.out.println("回调异常：" + e.getMessage());
    }

    @Override
    public Executor getExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
