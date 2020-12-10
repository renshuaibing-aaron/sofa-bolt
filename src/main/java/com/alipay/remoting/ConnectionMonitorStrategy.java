package com.alipay.remoting;

import com.alipay.remoting.util.RunStateRecordedFutureTask;

import java.util.List;
import java.util.Map;

/**
 * The strategy of connection monitor
 *
 * @author tsui
 * @version $Id: ConnectionMonitorStrategy.java, v 0.1 2017-02-21 12:06 tsui Exp $
 */
public interface ConnectionMonitorStrategy {

    /**
     * Filter connections to monitor
     *
     * @param connections
     */
    public Map<String, List<Connection>> filter(List<Connection> connections);

    /**
     * Add a set of connections to monitor.
     * <p>
     * The previous connections in monitor of this protocol,
     * will be dropped by monitor automatically.
     *
     * @param connPools
     */
    public void monitor(Map<String, RunStateRecordedFutureTask<ConnectionPool>> connPools);
}
