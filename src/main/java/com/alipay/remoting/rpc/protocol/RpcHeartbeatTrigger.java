package com.alipay.remoting.rpc.protocol;

import com.alipay.remoting.*;
import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.rpc.DefaultInvokeFuture;
import com.alipay.remoting.rpc.HeartbeatCommand;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.util.RemotingUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Handler for heart beat.
 *  客户端真正的空闲处理器，也叫心跳触发器 (注意这里是客户端使用的)
 * @author jiangping
 * @version $Id: RpcHeartbeatTrigger.java, v 0.1 2015-9-29 PM3:17:45 tao Exp $
 */
public class RpcHeartbeatTrigger implements HeartbeatTrigger {
    /**
     * max trigger times
     */
    // max trigger times，心跳最多多少次没响应，则关闭连接，默认为3
    // -Dbolt.tcp.heartbeat.maxtimes=3
    public static final Integer maxCount = ConfigManager.tcp_idle_maxtimes();

    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    // 心跳响应返回的超时时间，发送请求后1s内没有接收到响应就触发超时逻辑
    private static final long heartbeatTimeoutMillis = 1000;

    private CommandFactory commandFactory;

    public RpcHeartbeatTrigger(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * 心跳出发器
     * @see com.alipay.remoting.HeartbeatTrigger#heartbeatTriggered(io.netty.channel.ChannelHandlerContext)
     */
    @Override
    public void heartbeatTriggered(final ChannelHandlerContext ctx) throws Exception {

        //已经心跳的次数，默认为0
        Integer heartbeatTimes = ctx.channel().attr(Connection.HEARTBEAT_COUNT).get();

        final Connection conn = ctx.channel().attr(Connection.CONNECTION).get();

        // 心跳次数已经超过3次，直接关闭连接
        if (heartbeatTimes >= maxCount) {
            try {
                conn.close();
                logger.error(
                        "Heartbeat failed for {} times, close the connection from client side: {} ",
                        heartbeatTimes, RemotingUtil.parseRemoteAddress(ctx.channel()));
            } catch (Exception e) {
                logger.warn("Exception caught when closing connection in SharableHandler.", e);
            }
        } else {
            //检测该连接的心跳开关是否打开（只针对当前的 Connection 实例，不是全局的）
            boolean heartbeatSwitch = ctx.channel().attr(Connection.HEARTBEAT_SWITCH).get();
            if (!heartbeatSwitch) {
                return;
            }

            // 创建心跳命令
            final HeartbeatCommand heartbeat = new HeartbeatCommand();

            // 创建 InvokeFuture
            final InvokeFuture future = new DefaultInvokeFuture(heartbeat.getId(),
                    new InvokeCallbackListener() {
                        @Override
                        public void onResponse(InvokeFuture future) {

                            ResponseCommand response;
                            try {

                                // 获取响应
                                response = (ResponseCommand) future.waitResponse(0);
                            } catch (InterruptedException e) {
                                logger.error("Heartbeat ack process error! Id={}, from remoteAddr={}",
                                        heartbeat.getId(), RemotingUtil.parseRemoteAddress(ctx.channel()),
                                        e);
                                return;
                            }
                            if (response != null
                                    && response.getResponseStatus() == ResponseStatus.SUCCESS) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Heartbeat ack received! Id={}, from remoteAddr={}",
                                            response.getId(),
                                            RemotingUtil.parseRemoteAddress(ctx.channel()));
                                }
                                // 接收到正常心跳响应，将该连接的已心跳次数置为0
                                ctx.channel().attr(Connection.HEARTBEAT_COUNT).set(0);
                            } else {
                                if (response == null) {
                                    logger.error("Heartbeat timeout! The address is {}",
                                            RemotingUtil.parseRemoteAddress(ctx.channel()));
                                } else {
                                    logger.error(
                                            "Heartbeat exception caught! Error code={}, The address is {}",
                                            response.getResponseStatus(),
                                            RemotingUtil.parseRemoteAddress(ctx.channel()));
                                }
                                Integer times = ctx.channel().attr(Connection.HEARTBEAT_COUNT).get();

                                // 接收到错误的心跳响应，将该连接的已心跳次数+1
                                ctx.channel().attr(Connection.HEARTBEAT_COUNT).set(times + 1);
                            }
                        }

                        @Override
                        public String getRemoteAddress() {
                            return ctx.channel().remoteAddress().toString();
                        }
                    }, null, heartbeat.getProtocolCode().getFirstByte(), this.commandFactory);
            final int heartbeatId = heartbeat.getId();

            // 将 InvokeFuture 加入连接
            conn.addInvokeFuture(future);
            if (logger.isDebugEnabled()) {
                logger.debug("Send heartbeat, successive count={}, Id={}, to remoteAddr={}",
                        heartbeatTimes, heartbeatId, RemotingUtil.parseRemoteAddress(ctx.channel()));
            }
            // 发送 heartbeat
            System.out.println("【客户端发送心跳】");
            ctx.writeAndFlush(heartbeat).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Send heartbeat done! Id={}, to remoteAddr={}",
                                    heartbeatId, RemotingUtil.parseRemoteAddress(ctx.channel()));
                        }
                    } else {
                        logger.error("Send heartbeat failed! Id={}, to remoteAddr={}", heartbeatId,
                                RemotingUtil.parseRemoteAddress(ctx.channel()));
                    }
                }
            });

            // 设置超时任务（1s内没有接收到心跳响应，则直接返回超时失败响应，实现快速失败）
            TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    InvokeFuture future = conn.removeInvokeFuture(heartbeatId);
                    if (future != null) {
                        // 构造超时响应
                        future.putResponse(commandFactory.createTimeoutResponse(conn
                                .getRemoteAddress()));

                        // 回调
                        future.tryAsyncExecuteInvokeCallbackAbnormally();
                    }
                }
            }, heartbeatTimeoutMillis, TimeUnit.MILLISECONDS);
        }

    }
}
