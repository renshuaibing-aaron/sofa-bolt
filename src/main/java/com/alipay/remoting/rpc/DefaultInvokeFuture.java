package com.alipay.remoting.rpc;

import com.alipay.remoting.*;
import com.alipay.remoting.log.BoltLoggerFactory;
import io.netty.util.Timeout;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The default implementation of InvokeFuture.
 *
 * @author jiangping
 * @version $Id: DefaultInvokeFuture.java, v 0.1 2015-9-27 PM6:30:22 tao Exp $
 */
public class DefaultInvokeFuture implements InvokeFuture {

    private static final Logger logger = BoltLoggerFactory
            .getLogger("RpcRemoting");

    // 阻塞器
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);

    //消息唯一id
    private int invokeId;

    // 回调监听器
    private InvokeCallbackListener callbackListener;

    // 实际的回调函数
    private InvokeCallback callback;

    // 最终响应
    private volatile ResponseCommand responseCommand;

    // netty超时执行器
    private Timeout timeout;

    // 异常
    private Throwable cause;

    private ClassLoader classLoader;

    // 私有协议
    private byte protocol;

    private InvokeContext invokeContext;

    private CommandFactory commandFactory;

    /**
     * Constructor.
     *
     * @param invokeId         invoke id
     * @param callbackListener callback listener
     * @param callback         callback
     * @param protocol         protocol code
     * @param commandFactory   command factory
     */
    public DefaultInvokeFuture(int invokeId, InvokeCallbackListener callbackListener,
                               InvokeCallback callback, byte protocol, CommandFactory commandFactory) {
        this.invokeId = invokeId;
        this.callbackListener = callbackListener;
        this.callback = callback;
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.protocol = protocol;
        this.commandFactory = commandFactory;
    }

    /**
     * Constructor.
     *
     * @param invokeId         invoke id
     * @param callbackListener callback listener
     * @param callback         callback
     * @param protocol         protocol
     * @param commandFactory   command factory
     * @param invokeContext    invoke context
     */
    public DefaultInvokeFuture(int invokeId, InvokeCallbackListener callbackListener,
                               InvokeCallback callback, byte protocol,
                               CommandFactory commandFactory, InvokeContext invokeContext) {
        this(invokeId, callbackListener, callback, protocol, commandFactory);
        this.invokeContext = invokeContext;
    }

    @Override
    public ResponseCommand waitResponse(long timeoutMillis) throws InterruptedException {

        //  ... 服务端处理并返回响应 ...
   //   ... 客户端 netty worker 线程接收响应并填充到指定 invokeId 的 InvokeFuture 中，唤醒如下流程 ...
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    @Override
    public ResponseCommand waitResponse() throws InterruptedException {
        this.countDownLatch.await();
        return this.responseCommand;
    }

    @Override
    public RemotingCommand createConnectionClosedResponse(InetSocketAddress responseHost) {
        return this.commandFactory.createConnectionClosedResponse(responseHost, null);
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#putResponse(com.alipay.remoting.RemotingCommand)
     */
    @Override
    public void putResponse(RemotingCommand response) {
        //// 解锁等待（对于callback模式此处没有等待）
        this.responseCommand = (ResponseCommand) response;
        this.countDownLatch.countDown();
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#isDone()
     */
    @Override
    public boolean isDone() {
        return this.countDownLatch.getCount() <= 0;
    }

    @Override
    public ClassLoader getAppClassLoader() {
        return this.classLoader;
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#invokeId()
     */
    @Override
    public int invokeId() {
        return this.invokeId;
    }

    @Override
    public void executeInvokeCallback() {

        //执行相应的 callbackListener
        if (callbackListener != null) {
            if (this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
                callbackListener.onResponse(this);
            }
        }
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#getInvokeCallback()
     */
    @Override
    public InvokeCallback getInvokeCallback() {
        return this.callback;
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#addTimeout(io.netty.util.Timeout)
     */
    @Override
    public void addTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#cancelTimeout()
     */
    @Override
    public void cancelTimeout() {
        if (this.timeout != null) {
            this.timeout.cancel();
        }
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#getCause()
     */
    @Override
    public Throwable getCause() {
        return this.cause;
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#setCause(java.lang.Throwable)
     */
    @Override
    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#getProtocolCode()
     */
    @Override
    public byte getProtocolCode() {
        return this.protocol;
    }

    /**
     * @see InvokeFuture#setInvokeContext(InvokeContext)
     */
    @Override
    public InvokeContext getInvokeContext() {
        return invokeContext;
    }

    /**
     * @see InvokeFuture#getInvokeContext()
     */
    @Override
    public void setInvokeContext(InvokeContext invokeContext) {
        this.invokeContext = invokeContext;
    }

    /**
     * @see com.alipay.remoting.InvokeFuture#tryAsyncExecuteInvokeCallbackAbnormally()
     */
    @Override
    public void tryAsyncExecuteInvokeCallbackAbnormally() {
        try {
            Protocol protocol = ProtocolManager.getProtocol(ProtocolCode.fromBytes(this.protocol));
            if (null != protocol) {
                CommandHandler commandHandler = protocol.getCommandHandler();
                if (null != commandHandler) {
                    ExecutorService executor = commandHandler.getDefaultExecutor();
                    if (null != executor) {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                ClassLoader oldClassLoader = null;
                                try {
                                    if (DefaultInvokeFuture.this.getAppClassLoader() != null) {
                                        oldClassLoader = Thread.currentThread()
                                                .getContextClassLoader();
                                        Thread.currentThread().setContextClassLoader(
                                                DefaultInvokeFuture.this.getAppClassLoader());
                                    }
                                    DefaultInvokeFuture.this.executeInvokeCallback();
                                } finally {
                                    if (null != oldClassLoader) {
                                        Thread.currentThread()
                                                .setContextClassLoader(oldClassLoader);
                                    }
                                }
                            }
                        });
                    }
                } else {
                    logger.error("Executor null in commandHandler of protocolCode [{}].",
                            this.protocol);
                }
            } else {
                logger.error("protocolCode [{}] not registered!", this.protocol);
            }
        } catch (Exception e) {
            logger.error("Exception caught when executing invoke callback abnormally.", e);
        }
    }

}
