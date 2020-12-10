package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.*;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.HeartbeatAckCommand;
import com.alipay.remoting.rpc.HeartbeatCommand;
import com.alipay.remoting.util.RemotingUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;

/**
 * Processor for heart beat.
 *心跳命令处理器
 * 心跳命令处理器，处理 HeartbeatCommand 和 HeartbeatAckCommand 两种心跳请求
 * todo 心跳的处理和响应消息的处理都是一样的，如果 RemotingProcessor#executor 存在，则使用该线程池执行，
 *   否则使用 ProcessorManager#defaultExecutor 执行
 * @author tsui
 * @version $Id: RpcHeartBeatProcessor.java, v 0.1 2018-03-29 11:02 tsui Exp $
 */
public class RpcHeartBeatProcessor extends AbstractRemotingProcessor {
    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    @Override
    public void doProcess(final RemotingContext ctx, RemotingCommand msg) {

        //如果是心跳请求
        if (msg instanceof HeartbeatCommand) {// process the heartbeat
            final int id = msg.getId();
            if (logger.isDebugEnabled()) {
                logger.debug("Heartbeat received! Id=" + id + ", from "
                        + RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            }
            //构造心跳响应
            HeartbeatAckCommand ack = new HeartbeatAckCommand();
            ack.setId(id);

            // 发送心跳响应
            ctx.writeAndFlush(ack).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Send heartbeat ack done! Id={}, to remoteAddr={}", id,
                                    RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
                        }
                    } else {
                        logger.error("Send heartbeat ack failed! Id={}, to remoteAddr={}", id,
                                RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
                    }
                }

            });

        } else if (msg instanceof HeartbeatAckCommand) {
            //如果是心跳响应
            Connection conn = ctx.getChannelContext().channel().attr(Connection.CONNECTION).get();
            InvokeFuture future = conn.removeInvokeFuture(msg.getId());
            if (future != null) {
                // 设置心跳响应消息
                future.putResponse(msg);
                // 取消超时任务
                future.cancelTimeout();
                try {
                    // 回调
                    future.executeInvokeCallback();
                } catch (Exception e) {
                    logger.error(
                            "Exception caught when executing heartbeat invoke callback. From {}",
                            RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()), e);
                }
            } else {
                logger
                        .warn(
                                "Cannot find heartbeat InvokeFuture, maybe already timeout. Id={}, From {}",
                                msg.getId(),
                                RemotingUtil.parseRemoteAddress(ctx.getChannelContext().channel()));
            }
        } else {
            throw new RuntimeException("Cannot process command: " + msg.getClass().getName());
        }
    }

}