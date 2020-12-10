package com.alipay.remoting;

import java.util.List;

/**
 * Select strategy from connection pool
 *
 * @author xiaomin.cxm
 * @version $Id: ConnectionSelectStrategy.java, v 0.1 Mar 14, 2016 11:06:57 AM xiaomin.cxm Exp $
 */
public interface ConnectionSelectStrategy {
    /**
     * select strategy
     *
     * @param conns
     * @return
     */
    Connection select(List<Connection> conns);
}