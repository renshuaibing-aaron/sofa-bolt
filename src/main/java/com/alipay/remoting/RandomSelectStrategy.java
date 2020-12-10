package com.alipay.remoting;

import com.alipay.remoting.config.Configs;
import com.alipay.remoting.config.switches.GlobalSwitch;
import com.alipay.remoting.log.BoltLoggerFactory;
import com.alipay.remoting.util.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Select a connection randomly
 *
 * @author yunliang.shi
 * @version $Id: RandomSelectStrategy.java, v 0.1 Mar 30, 2016 8:38:40 PM yunliang.shi Exp $
 */
public class RandomSelectStrategy implements ConnectionSelectStrategy {
    /**
     * logger
     */
    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");

    /**
     * max retry times
     */
    private static final int MAX_TIMES = 5;

    /**
     * random
     */
    private final Random random = new Random();

    private GlobalSwitch globalSwitch;

    public RandomSelectStrategy() {
    }

    public RandomSelectStrategy(GlobalSwitch globalSwitch) {
        this.globalSwitch = globalSwitch;
    }

    /**
     * @see com.alipay.remoting.ConnectionSelectStrategy#select(java.util.List)
     */
    @Override
    public Connection select(List<Connection> conns) {
        try {
            if (conns == null) {
                return null;
            }
            int size = conns.size();
            if (size == 0) {
                return null;
            }

            Connection result = null;
            // 如果开启了 连接监控，则从状态为健康的连接中获取连接；否则直接从 conns 全连接中随机获取
            if (null != this.globalSwitch
                    && this.globalSwitch.isOn(GlobalSwitch.CONN_MONITOR_SWITCH)) {
                List<Connection> serviceStatusOnConns = new ArrayList<Connection>();
                for (Connection conn : conns) {
                    String serviceStatus = (String) conn.getAttribute(Configs.CONN_SERVICE_STATUS);
                    if (!StringUtils.equals(serviceStatus, Configs.CONN_SERVICE_STATUS_OFF)) {
                        serviceStatusOnConns.add(conn);
                    }
                }
                if (serviceStatusOnConns.size() == 0) {
                    throw new Exception(
                            "No available connection when select in RandomSelectStrategy.");
                }
                result = randomGet(serviceStatusOnConns);
            } else {
                result = randomGet(conns);
            }
            return result;
        } catch (Throwable e) {
            logger.error("Choose connection failed using RandomSelectStrategy!", e);
            return null;
        }
    }

    /**
     * get one connection randomly
     *
     * @param conns
     * @return
     */
    private Connection randomGet(List<Connection> conns) {
        if (null == conns || conns.isEmpty()) {
            return null;
        }

        int size = conns.size();
        int tries = 0;
        Connection result = null;
        // 不断重试获取状态为可用的 Connection
        while ((result == null || !result.isFine()) && tries++ < MAX_TIMES) {
            result = conns.get(this.random.nextInt(size));
        }

        if (result != null && !result.isFine()) {
            result = null;
        }
        return result;
    }
}
