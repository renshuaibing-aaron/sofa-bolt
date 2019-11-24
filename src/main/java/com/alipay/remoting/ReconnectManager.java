/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.remoting;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.log.BoltLoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reconnect manager.
 *
 * @author yunliang.shi
 * @version $Id: ReconnectManager.java, v 0.1 Mar 11, 2016 5:20:50 PM yunliang.shi Exp $
 */
public class ReconnectManager {
    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");

    // 取消重连的队列
    protected final List<Url/* url */> canceled = new CopyOnWriteArrayList<Url>();

    // 待重连队列
    private final LinkedBlockingQueue<ReconnectTask> tasks = new LinkedBlockingQueue<ReconnectTask>();


    private final Thread healConnectionThreads;
    private volatile boolean started;

    private int healConnectionInterval = 1000;
    private ConnectionManager connectionManager;

    public ReconnectManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;

        // 创建重连任务
        this.healConnectionThreads = new Thread(new HealConnectionRunner());

        // 启动重连线程
        this.healConnectionThreads.start();
        this.started = true;
    }

    // 执行重连操作
    private void doReconnectTask(ReconnectTask task) throws InterruptedException, RemotingException {
        connectionManager.createConnectionAndHealIfNeed(task.url);
    }

    // 将重连任务添加到待重连队列中
    private void addReconnectTask(ReconnectTask task) {
        tasks.add(task);
    }

    // 将不需要重连的任务添加到取消重连队列中
    public void addCancelUrl(Url url) {
        canceled.add(url);
    }

    public void removeCancelUrl(Url url) {
        canceled.remove(url);
    }

    /**
     * add reconnect task
     * // 将重连任务添加到待重连队列中
     * @param url
     */
    public void addReconnectTask(Url url) {
        ReconnectTask task = new ReconnectTask();
        task.url = url;
        tasks.add(task);
    }

    /**
     * Check task whether is valid, if canceled, is not valid
     * 检测 task 是否需要重连
     * @param task
     * @return
     */
    private boolean isValidTask(ReconnectTask task) {
        return !canceled.contains(task.url);
    }

    /**
     *  // 停止重连线程
     * stop reconnect thread
     */
    public void stop() {

        // 如果重连线程没启动过，直接返回
        if (!this.started) {
            return;
        }
        this.started = false;

        // 中断重连线程
        healConnectionThreads.interrupt();

        // 清空待重连队列和取消重连队列
        this.tasks.clear();
        this.canceled.clear();
    }

    class ReconnectTask {
        Url url;
    }

    /**
     * heal connection thread
     * // 重连任务
     * @author yunliang.shi
     * @version $Id: ReconnectManager.java, v 0.1 Mar 11, 2016 5:24:08 PM yunliang.shi Exp $
     */
    private final class HealConnectionRunner implements Runnable {
        private long lastConnectTime = -1;

        @Override
        public void run() {
            while (ReconnectManager.this.started) {
                long start = -1;
                ReconnectTask task = null;
                try {

                    // 如果重连线程执行的连接操作的时间小于 healConnectionInterval，当前线程睡 healConnectionInterval（防止待重连队列为空，线程空转，CPU消耗严重）
                    // 如果重连线程执行的连接操作的时间 >= healConnectionInterval，可继续执行
                    if (this.lastConnectTime > 0
                            && this.lastConnectTime < ReconnectManager.this.healConnectionInterval
                            || this.lastConnectTime < 0) {
                        Thread.sleep(ReconnectManager.this.healConnectionInterval);
                    }
                    try {
                        // 从待重连队列获取待重连任务
                        task = ReconnectManager.this.tasks.take();
                    } catch (InterruptedException e) {
                        // ignore
                    }

                    start = System.currentTimeMillis();
                    if (ReconnectManager.this.isValidTask(task)) {
                        try {
                            // 如果待重连任务没有被取消，则执行重连任务
                            ReconnectManager.this.doReconnectTask(task);
                        } catch (InterruptedException e) {
                            throw e;
                        }
                    } else {
                        logger.warn("Invalid reconnect request task {}, cancel list size {}",
                                task.url, canceled.size());
                    }
                    this.lastConnectTime = System.currentTimeMillis() - start;
                } catch (Exception e) {

                    // 如果失败，将失败任务重新加入待重连队列，之后重试重连操作
                    retryWhenException(start, task, e);
                }
            }
        }

        private void retryWhenException(long start, ReconnectTask task, Exception e) {
            if (start != -1) {
                this.lastConnectTime = System.currentTimeMillis() - start;
            }
            if (task != null) {
                logger.warn("reconnect target: {} failed.", task.url, e);
                // 将失败任务重新加入待重连队列
                ReconnectManager.this.addReconnectTask(task);
            }
        }
    }
}
