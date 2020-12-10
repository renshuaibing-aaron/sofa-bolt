package com.alipay.remoting;

/**
 * Connection heart beat manager, operate heart beat whether enabled for a certain connection at runtime
 *用于开启或关闭 Connection 的心跳逻辑
 * @author xiaomin.cxm
 * @version $Id: ConnectionHeartbeatManager.java, v 0.1 Apr 12, 2016 6:55:56 PM xiaomin.cxm Exp $
 */
public interface ConnectionHeartbeatManager {

    /**
     * disable heart beat for a certain connection
     *
     * @param connection
     */
    void disableHeartbeat(Connection connection);

    /**
     * enable heart beat for a certain connection
     *
     * @param connection
     */
    void enableHeartbeat(Connection connection);
}
