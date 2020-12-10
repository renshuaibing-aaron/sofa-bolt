package com.alipay.remoting.util;

import com.alipay.remoting.InvokeContext;
import org.slf4j.Logger;

/**
 *
 * Trace log util
 *
 * @author tsui
 * @version $Id: TraceLogUtil.java, v 0.1 2016-08-02 17:31 tsui Exp $
 */
public class TraceLogUtil {
    /**
     * print trace log
     *
     * @param traceId
     * @param invokeContext
     */
    public static void printConnectionTraceLog(Logger logger, String traceId,
                                               InvokeContext invokeContext) {
        String sourceIp = invokeContext.get(InvokeContext.CLIENT_LOCAL_IP);
        Integer sourcePort = invokeContext.get(InvokeContext.CLIENT_LOCAL_PORT);
        String targetIp = invokeContext.get(InvokeContext.CLIENT_REMOTE_IP);
        Integer targetPort = invokeContext.get(InvokeContext.CLIENT_REMOTE_PORT);
        StringBuilder logMsg = new StringBuilder();
        logMsg.append(traceId).append(",");
        logMsg.append(sourceIp).append(",");
        logMsg.append(sourcePort).append(",");
        logMsg.append(targetIp).append(",");
        logMsg.append(targetPort);
        if (logger.isInfoEnabled()) {
            logger.info(logMsg.toString());
        }
    }
}