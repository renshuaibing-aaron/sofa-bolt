package com.alipay.remoting;

/**
 * Process connection events.
 *
 * @author jiangping
 * @version $Id: ConnectionEventProcessor.java, v 0.1 Mar 5, 2016 11:01:07 AM tao Exp $
 */
public interface ConnectionEventProcessor {
    /**
     * Process event.<br>
     *
     * @param remoteAddr
     * @param conn
     */
    public void onEvent(String remoteAddr, Connection conn);
}
