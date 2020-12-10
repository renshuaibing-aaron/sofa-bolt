package com.alipay.remoting;

import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.RunStateRecordedFutureTask;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A default connection monitor that handle connections with strategies
 *
 * @author tsui
 * @version $Id: DefaultConnectionMonitor.java, v 0.1 2017-02-21 12:09 tsui Exp $
 */
public class DefaultConnectionMonitor {

    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");

    /**
     * Connection pools to monitor
     */
    private DefaultConnectionManager connectionManager;

    /**
     * Monitor strategy
     */
    private ConnectionMonitorStrategy strategy;

    private ScheduledThreadPoolExecutor executor;

    public DefaultConnectionMonitor(ConnectionMonitorStrategy strategy,
                                    DefaultConnectionManager connectionManager) {
        this.strategy = strategy;
        this.connectionManager = connectionManager;
    }

    /**
     * Start schedule task
     */
    public void start() {
        /** initial delay to execute schedule task, unit: ms */
        long initialDelay = ConfigManager.conn_monitor_initial_delay();

        /** period of schedule task, unit: ms*/
        long period = ConfigManager.conn_monitor_period();

        this.executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(
                "ConnectionMonitorThread", true), new ThreadPoolExecutor.AbortPolicy());
        MonitorTask monitorTask = new MonitorTask();
        this.executor.scheduleAtFixedRate(monitorTask, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * cancel task and shutdown executor
     *
     * @throws Exception
     */
    public void destroy() {
        executor.purge();
        executor.shutdown();
    }

    /**
     * Monitor Task
     *
     * @author tsui
     * @version $Id: DefaultConnectionMonitor.java, v 0.1 2017-02-21 12:09 tsui Exp $
     */
    private class MonitorTask implements Runnable {
        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                if (strategy != null) {
                    Map<String, RunStateRecordedFutureTask<ConnectionPool>> connPools = connectionManager
                            .getConnPools();
                    strategy.monitor(connPools);
                }
            } catch (Exception e) {
                logger.warn("MonitorTask error", e);
            }
        }
    }
}
