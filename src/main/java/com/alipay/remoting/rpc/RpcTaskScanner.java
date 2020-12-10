package com.alipay.remoting.rpc;

import com.alipay.remoting.NamedThreadFactory;
import com.alipay.remoting.Scannable;
import com.alipay.remoting.log.BoltLoggerFactory;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Scanner is used to do scan task.
 *Bolt 提供的一个统一的扫描器，用于执行一些后台任务
 * @author jiangping
 * @version $Id: RpcTaskScanner.java, v 0.1 Mar 4, 2016 3:30:52 PM tao Exp $
 */
public class RpcTaskScanner {
    private static final Logger logger = BoltLoggerFactory.getLogger("RpcRemoting");

    private ScheduledExecutorService scheduledService = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory(
                    "RpcTaskScannerThread", true));

    private List<Scannable> scanList = new LinkedList<Scannable>();

    /**
     * Start!
     */
    public void start() {
        scheduledService.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                for (Scannable scanned : scanList) {
                    try {
                        scanned.scan();
                    } catch (Throwable t) {
                        logger.error("Exception caught when scannings.", t);
                    }
                }
            }

        }, 10000, 10000, TimeUnit.MILLISECONDS);
    }

    /**
     * Add scan target.
     *
     * @param target
     */
    public void add(Scannable target) {
        scanList.add(target);
    }

    /**
     * Shutdown the scheduled service.
     */
    public void shutdown() {
        scheduledService.shutdown();
    }

}
