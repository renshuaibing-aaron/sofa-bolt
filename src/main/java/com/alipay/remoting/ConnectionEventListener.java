package com.alipay.remoting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listen and dispatch connection events.
 *监听器
 *   Connection 事件监听器，存储处理对应 ConnectionEventType 的 ConnectionEventProcessor 列表
 * @author jiangping
 * @version $Id: DefaultConnectionEventListener.java, v 0.1 Mar 5, 2016 10:56:20 AM tao Exp $
 */
public class ConnectionEventListener {

    private ConcurrentHashMap<ConnectionEventType, List<ConnectionEventProcessor>> processors = new ConcurrentHashMap<ConnectionEventType, List<ConnectionEventProcessor>>(
            3);

    /**
     * Dispatch events.
     *
     * @param type
     * @param remoteAddr
     * @param conn
     */
    public void onEvent(ConnectionEventType type, String remoteAddr, Connection conn) {
        List<ConnectionEventProcessor> processorList = this.processors.get(type);
        if (processorList != null) {
            for (ConnectionEventProcessor processor : processorList) {
                processor.onEvent(remoteAddr, conn);
            }
        }
    }

    /**
     * Add event processor.
     *
     * @param type
     * @param processor
     */
    public void addConnectionEventProcessor(ConnectionEventType type,
                                            ConnectionEventProcessor processor) {
        List<ConnectionEventProcessor> processorList = this.processors.get(type);
        if (processorList == null) {
            this.processors.putIfAbsent(type, new ArrayList<ConnectionEventProcessor>(1));
            processorList = this.processors.get(type);
        }
        processorList.add(processor);
    }

}
