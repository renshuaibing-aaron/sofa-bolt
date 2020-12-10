package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.*;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RemotingUtil;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;

/**
 * Processor to process RpcResponse.
 *
 * @author jiangping
 * @version $Id: RpcResponseProcessor.java, v 0.1 2015-10-1 PM11:06:52 tao Exp $
 */
public class RpcResponseProcessor extends AbstractRemotingProcessor<RemotingCommand> {

    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    /**
     * Default constructor.
     */
    public RpcResponseProcessor() {

    }

    /**
     * Constructor.
     */
    public RpcResponseProcessor(ExecutorService executor) {
        super(executor);
    }

    /**
     * @see com.alipay.remoting.AbstractRemotingProcessor#doProcess
     */
    @Override
    public void doProcess(RemotingContext ctx, RemotingCommand cmd) {

        //cmd是RpcResponseCommand
        Connection conn = ctx.getChannelContext().channel().attr(Connection.CONNECTION).get();

        // 从连接中根据响应id获取请求的 InvokeFuture
        InvokeFuture future = conn.removeInvokeFuture(cmd.getId());

        //填充响应 + 取消 io.netty.util.TimerTask 超时任务
        ClassLoader oldClassLoader = null;
        try {
            if (future != null) {
                if (future.getAppClassLoader() != null) {
                    oldClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(future.getAppClassLoader());
                }
                //填充响应 + 唤醒主线程 + 如果有回调，调用回调
                future.putResponse(cmd);

                //取消 io.netty.util.TimerTask 超时任务
                future.cancelTimeout();
                try {
                    //调用回调
                    future.executeInvokeCallback();
                } catch (Exception e) {
                    logger.error("Exception caught when executing invoke callback, id={}",
                            cmd.getId(), e);
                }
            } else {
                logger.warn("Cannot find InvokeFuture, maybe already timeout, id={}, from={} ",
                                cmd.getId(),
                                RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            }
        } finally {
            if (null != oldClassLoader) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }

    }

}
