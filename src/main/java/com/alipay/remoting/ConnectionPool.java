package com.alipay.remoting;

import com.alipay.remoting.log.BoltLoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Connection pool
 *ConnectionPool 连接池：存储 { uniqueKey, List<Connection> } ，
 * uniqueKey 默认为 ip:port；包含 ConnectionSelectStrategy，从 pool 中选择 Connection
 * @author xiaomin.cxm
 * @version $Id: ConnectionPool.java, v 0.1 Mar 8, 2016 11:04:54 AM xiaomin.cxm Exp $
 */
public class ConnectionPool implements Scannable {
    // ~~~ constants
    /**
     * logger
     */
    private static final Logger logger = BoltLoggerFactory
            .getLogger("CommonDefault");

    /**
     * connections
     */
    private CopyOnWriteArrayList<Connection> conns = new CopyOnWriteArrayList<Connection>();

    /**
     * strategy
     */
    private ConnectionSelectStrategy strategy;

    /**
     * timestamp to record the last time this pool be accessed
     */
    private volatile long lastAccessTimestamp;

    /**
     * whether async create connection done
     */
    private volatile boolean asyncCreationDone = true;

    /**
     * Constructor
     *
     * @param strategy
     */
    public ConnectionPool(ConnectionSelectStrategy strategy) {
        this.strategy = strategy;
    }

    // ~~~ members

    /**
     * add a connection
     *
     * @param connection
     */
    public void add(Connection connection) {
        markAccess();
        if (null == connection) {
            return;
        }
        boolean res = this.conns.addIfAbsent(connection);
        if (res) {
            connection.increaseRef();
        }
    }

    /**
     * check weather a connection already added
     *
     * @param connection
     * @return
     */
    public boolean contains(Connection connection) {
        return this.conns.contains(connection);
    }

    /**
     * removeAndTryClose a connection
     *
     * @param connection
     */
    public void removeAndTryClose(Connection connection) {
        if (null == connection) {
            return;
        }
        boolean res = this.conns.remove(connection);
        if (res) {
            connection.decreaseRef();
        }
        //// 如果该连接没有引用了，则 close 连接
        if (connection.noRef()) {
            connection.close();
        }
    }

    /**
     * remove all connections
     */
    public void removeAllAndTryClose() {
        for (Connection conn : this.conns) {
            removeAndTryClose(conn);
        }
        this.conns.clear();
    }

    /**
     * get a connection
     *
     * @return
     */
    public Connection get() {
        markAccess();
        if (null != this.conns) {
            List<Connection> snapshot = new ArrayList<Connection>(this.conns);
            if (snapshot.size() > 0) {
                // 使用 ConnectionSelectStrategy 从 List<Connection> 选一个 Connection
                return this.strategy.select(snapshot);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * get all connections
     *
     * @return
     */
    public List<Connection> getAll() {
        markAccess();
        return new ArrayList<Connection>(this.conns);
    }

    /**
     * connection pool size
     *
     * @return
     */
    public int size() {
        return this.conns.size();
    }

    /**
     * is connection pool empty
     *
     * @return
     */
    public boolean isEmpty() {
        return this.conns.isEmpty();
    }

    /**
     * Getter method for property <tt>lastAccessTimestamp</tt>.
     *
     * @return property value of lastAccessTimestamp
     */
    public long getLastAccessTimestamp() {
        return this.lastAccessTimestamp;
    }

    /**
     * do mark the time stamp when access this pool
     */
    private void markAccess() {
        this.lastAccessTimestamp = System.currentTimeMillis();
    }

    /**
     * is async create connection done
     *
     * @return
     */
    public boolean isAsyncCreationDone() {
        return this.asyncCreationDone;
    }

    /**
     * do mark async create connection done
     */
    public void markAsyncCreationDone() {
        this.asyncCreationDone = true;
    }

    /**
     * do mark async create connection start
     */
    public void markAsyncCreationStart() {
        this.asyncCreationDone = false;
    }

    /**
     * @see com.alipay.remoting.Scannable#scan()
     */
    @Override
    public void scan() {
        if (null != this.conns && !this.conns.isEmpty()) {
            for (Connection conn : conns) {
                if (!conn.isFine()) {
                    logger.warn(
                            "Remove bad connection when scanning conns of ConnectionPool - {}:{}",
                            conn.getRemoteIP(), conn.getRemotePort());
                    conn.close();
                    this.removeAndTryClose(conn);
                }
            }
        }
    }
}
