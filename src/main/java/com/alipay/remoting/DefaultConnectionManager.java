package com.alipay.remoting;

import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.config.switches.GlobalSwitch;
import com.alipay.remoting.connection.ConnectionFactory;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.util.FutureTaskUtil;
import com.alipay.remoting.util.RunStateRecordedFutureTask;
import com.alipay.remoting.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Abstract implementation of connection manager
 *
 * @author xiaomin.cxm
 * @version $Id: DefaultConnectionManager.java, v 0.1 Mar 8, 2016 10:43:51 AM xiaomin.cxm Exp $
 */
public class DefaultConnectionManager implements ConnectionManager, ConnectionHeartbeatManager,
        Scannable {

    // ~~~ constants
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger("CommonDefault");

    /**
     * default expire time to remove connection pool, time unit: milliseconds
     */
    private static final int DEFAULT_EXPIRE_TIME = 10 * 60 * 1000;

    /**
     * default retry times when falied to get result of FutureTask
     */
    private static final int DEFAULT_RETRY_TIMES = 2;

    // ~~~ members
    /**
     * connection pool initialize tasks
     */
    protected ConcurrentHashMap<String, RunStateRecordedFutureTask<ConnectionPool>> connTasks;
    /**
     * heal connection tasks
     */
    protected ConcurrentHashMap<String, FutureTask<Integer>> healTasks;
    /**
     * connection pool select strategy
     */
    protected ConnectionSelectStrategy connectionSelectStrategy;
    /**
     * address parser
     */
    protected RemotingAddressParser addressParser;
    /**
     * connection factory
     */
    protected ConnectionFactory connectionFactory;
    /**
     * connection event handler
     */
    protected ConnectionEventHandler connectionEventHandler;
    /**
     * connection event listener
     */
    protected ConnectionEventListener connectionEventListener;
    /**
     * min pool size for asyncCreateConnectionExecutor
     */
    private int minPoolSize = ConfigManager
            .conn_create_tp_min_size();
    /**
     * max pool size for asyncCreateConnectionExecutor
     */
    private int maxPoolSize = ConfigManager
            .conn_create_tp_max_size();
    /**
     * queue size for asyncCreateConnectionExecutor
     */
    private int queueSize = ConfigManager
            .conn_create_tp_queue_size();
    /**
     * keep alive time for asyncCreateConnectionExecutor
     */
    private long keepAliveTime = ConfigManager
            .conn_create_tp_keepalive();
    /**
     * executor initialie status
     */
    private volatile boolean executorInitialized;
    /**
     * executor to create connections in async way
     * note: this is lazy initialized
     */
    private Executor asyncCreateConnectionExecutor;
    /**
     * switch status
     */
    private GlobalSwitch globalSwitch;

    // ~~~ constructors

    /**
     * Default constructor
     */
    public DefaultConnectionManager() {
        this.connTasks = new ConcurrentHashMap<String, RunStateRecordedFutureTask<ConnectionPool>>();
        this.healTasks = new ConcurrentHashMap<String, FutureTask<Integer>>();
        this.connectionSelectStrategy = new RandomSelectStrategy(globalSwitch);
    }

    /**
     * @param connectionSelectStrategy
     */
    public DefaultConnectionManager(ConnectionSelectStrategy connectionSelectStrategy) {
        this();
        this.connectionSelectStrategy = connectionSelectStrategy;
    }

    /**
     * @param connectionSelectStrategy
     * @param connectionFactory
     */
    public DefaultConnectionManager(ConnectionSelectStrategy connectionSelectStrategy,
                                    ConnectionFactory connectionFactory) {
        this(connectionSelectStrategy);
        this.connectionFactory = connectionFactory;
    }

    /**
     * @param connectionFactory
     * @param addressParser
     * @param connectionEventHandler
     */
    public DefaultConnectionManager(ConnectionFactory connectionFactory,
                                    RemotingAddressParser addressParser,
                                    ConnectionEventHandler connectionEventHandler) {
        this(new RandomSelectStrategy(), connectionFactory);
        this.addressParser = addressParser;
        this.connectionEventHandler = connectionEventHandler;
    }

    /**
     * @param connectionSelectStrategy
     * @param connectionFactory
     * @param connectionEventHandler
     * @param connectionEventListener
     */
    public DefaultConnectionManager(ConnectionSelectStrategy connectionSelectStrategy,
                                    ConnectionFactory connectionFactory,
                                    ConnectionEventHandler connectionEventHandler,
                                    ConnectionEventListener connectionEventListener) {
        this(connectionSelectStrategy, connectionFactory);
        this.connectionEventHandler = connectionEventHandler;
        this.connectionEventListener = connectionEventListener;
    }

    /**
     * @param connectionSelectStrategy
     * @param connctionFactory
     * @param connectionEventHandler
     * @param connectionEventListener
     * @param globalSwitch
     */
    public DefaultConnectionManager(ConnectionSelectStrategy connectionSelectStrategy,
                                    ConnectionFactory connctionFactory,
                                    ConnectionEventHandler connectionEventHandler,
                                    ConnectionEventListener connectionEventListener,
                                    GlobalSwitch globalSwitch) {
        this(connectionSelectStrategy, connctionFactory, connectionEventHandler,
                connectionEventListener);
        this.globalSwitch = globalSwitch;
    }

    // ~~~ interface methods

    /**
     * @see com.alipay.remoting.ConnectionManager#init()
     */
    @Override
    public void init() {
        this.connectionEventHandler.setConnectionManager(this);
        this.connectionEventHandler.setConnectionEventListener(connectionEventListener);
        this.connectionFactory.init(connectionEventHandler);
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#add(com.alipay.remoting.Connection)
     */
    @Override
    public void add(Connection connection) {
        Set<String> poolKeys = connection.getPoolKeys();
        for (String poolKey : poolKeys) {
            this.add(connection, poolKey);
        }
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#add(com.alipay.remoting.Connection, java.lang.String)
     */
    @Override
    public void add(Connection connection, String poolKey) {
        ConnectionPool pool = null;
        try {
            // get or create an empty connection pool
            pool = this.getConnectionPoolAndCreateIfAbsent(poolKey, new ConnectionPoolCall());
        } catch (Exception e) {
            // should not reach here.
            logger.error(
                    "[NOTIFYME] Exception occurred when getOrCreateIfAbsent an empty ConnectionPool!",
                    e);
        }
        if (pool != null) {
            pool.add(connection);
        } else {
            // should not reach here.
            logger.error("[NOTIFYME] Connection pool NULL!");
        }
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#get(String)
     */
    @Override
    public Connection get(String poolKey) {
        ConnectionPool pool = this.getConnectionPool(this.connTasks.get(poolKey));
        return null == pool ? null : pool.get();
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#getAll(java.lang.String)
     */
    @Override
    public List<Connection> getAll(String poolKey) {
        ConnectionPool pool = this.getConnectionPool(this.connTasks.get(poolKey));
        return null == pool ? new ArrayList<Connection>() : pool.getAll();
    }

    /**
     * Get all connections of all poolKey.
     *
     * @return a map with poolKey as key and a list of connections in ConnectionPool as value
     */
    @Override
    public Map<String, List<Connection>> getAll() {
        Map<String, List<Connection>> allConnections = new HashMap<String, List<Connection>>();
        Iterator<Map.Entry<String, RunStateRecordedFutureTask<ConnectionPool>>> iterator = this
                .getConnPools().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, RunStateRecordedFutureTask<ConnectionPool>> entry = iterator.next();
            ConnectionPool pool = FutureTaskUtil.getFutureTaskResult(entry.getValue(), logger);
            if (null != pool) {
                allConnections.put(entry.getKey(), pool.getAll());
            }
        }
        return allConnections;
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#remove(com.alipay.remoting.Connection)
     */
    @Override
    public void remove(Connection connection) {
        if (null == connection) {
            return;
        }
        Set<String> poolKeys = connection.getPoolKeys();
        if (null == poolKeys || poolKeys.isEmpty()) {
            connection.close();
            logger.warn("Remove and close a standalone connection.");
        } else {
            for (String poolKey : poolKeys) {
                this.remove(connection, poolKey);
            }
        }
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#remove(com.alipay.remoting.Connection, java.lang.String)
     */
    @Override
    public void remove(Connection connection, String poolKey) {
        if (null == connection || StringUtils.isBlank(poolKey)) {
            return;
        }
        ConnectionPool pool = this.getConnectionPool(this.connTasks.get(poolKey));
        if (null == pool) {
            connection.close();
            logger.warn("Remove and close a standalone connection.");
        } else {
            pool.removeAndTryClose(connection);
            if (pool.isEmpty()) {
                this.removeTask(poolKey);
                logger.warn(
                        "Remove and close the last connection in ConnectionPool with poolKey {}",
                        poolKey);
            } else {
                logger
                        .warn(
                                "Remove and close a connection in ConnectionPool with poolKey {}, {} connections left.",
                                poolKey, pool.size());
            }
        }
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#remove(java.lang.String)
     */
    @Override
    public void remove(String poolKey) {
        if (StringUtils.isBlank(poolKey)) {
            return;
        }

        RunStateRecordedFutureTask<ConnectionPool> task = this.connTasks.remove(poolKey);
        if (null != task) {
            ConnectionPool pool = this.getConnectionPool(task);
            if (null != pool) {
                pool.removeAllAndTryClose();
                logger.warn("Remove and close all connections in ConnectionPool of poolKey={}",
                        poolKey);
            }
        }
    }

    /**
     * Warning! This is weakly consistent implementation, to prevent lock the whole {@link ConcurrentHashMap}.
     *
     * @see ConnectionManager#removeAll()
     */
    @Override
    public void removeAll() {
        if (null == this.connTasks || this.connTasks.isEmpty()) {
            return;
        }
        if (null != this.connTasks && !this.connTasks.isEmpty()) {
            Iterator<String> iter = this.connTasks.keySet().iterator();
            while (iter.hasNext()) {
                String poolKey = iter.next();
                this.removeTask(poolKey);
                iter.remove();
            }
            logger.warn("All connection pool and connections have been removed!");
        }
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#check(com.alipay.remoting.Connection)
     */
    @Override
    public void check(Connection connection) throws RemotingException {
        if (connection == null) {
            throw new RemotingException("Connection is null when do check!");
        }
        if (connection.getChannel() == null || !connection.getChannel().isActive()) {
            this.remove(connection);
            throw new RemotingException("Check connection failed for address: "
                    + connection.getUrl());
        }
        if (!connection.getChannel().isWritable()) {
            // No remove. Most of the time it is unwritable temporarily.
            throw new RemotingException("Check connection failed for address: "
                    + connection.getUrl() + ", maybe write overflow!");
        }
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#count(java.lang.String)
     */
    @Override
    public int count(String poolKey) {
        if (StringUtils.isBlank(poolKey)) {
            return 0;
        }
        ConnectionPool pool = this.getConnectionPool(this.connTasks.get(poolKey));
        if (null != pool) {
            return pool.size();
        } else {
            return 0;
        }
    }

    /**
     * in case of cache pollution and connection leak, to do schedule scan
     *
     * @see com.alipay.remoting.Scannable#scan()
     */
    @Override
    public void scan() {
        if (null != this.connTasks && !this.connTasks.isEmpty()) {
            Iterator<String> iter = this.connTasks.keySet().iterator();
            while (iter.hasNext()) {
                String poolKey = iter.next();
                ConnectionPool pool = this.getConnectionPool(this.connTasks.get(poolKey));
                if (null != pool) {
                    pool.scan();
                    if (pool.isEmpty()) {
                        if ((System.currentTimeMillis() - pool.getLastAccessTimestamp()) > DEFAULT_EXPIRE_TIME) {
                            iter.remove();
                            logger.warn("Remove expired pool task of poolKey {} which is empty.",
                                    poolKey);
                        }
                    }
                }
            }
        }
    }

    /**
     * If no task cached, create one and initialize the connections.
     *
     * @see ConnectionManager#getAndCreateIfAbsent(Url)
     */
    @Override
    public Connection getAndCreateIfAbsent(Url url) throws InterruptedException, RemotingException {
        // get and create a connection pool with initialized connections.
        //System.out.println("创建一个 Callable<ConnectionPool> task");

        //参数：poolkey: 127.0.0.1:8999 callable: 上述的 ConnectionPoolCall 实例
        // 创建一个 Callable<ConnectionPool> task
        ConnectionPoolCall connectionPoolCall = new ConnectionPoolCall(url);

        //1.1 获取或者创建 poolKey 的 ConnectionPool
        ConnectionPool pool = this.getConnectionPoolAndCreateIfAbsent(url.getUniqueKey(),connectionPoolCall );


        if (null != pool) {
            //从 ConnectionPool 中使用 ConnectionSelectStrategy 获取一个 Connection
            return pool.get();
        } else {
            logger.error("[NOTIFYME] bug detected! pool here must not be null!");
            return null;
        }
    }

    /**
     * If no task cached, create one and initialize the connections.
     * If task cached, check whether the number of connections adequate, if not then heal it.
     *
     * @param url
     * @return
     * @throws InterruptedException
     * @throws RemotingException
     */
    @Override
    public void createConnectionAndHealIfNeed(Url url) throws InterruptedException,
            RemotingException {

        // 这里使用了建连操作方法，
        // 如果 ConnectionPool 不存在，则创建 ConnectionPool，然后创建指定数量的 Connection；
        // 如果 ConnectionPool 已经存在，那么这里会直接获取 ConnectionPool 并返回，此时就有可能需要重连操作
        // get and create a connection pool with initialized connections.
        ConnectionPool pool = this.getConnectionPoolAndCreateIfAbsent(url.getUniqueKey(),
                new ConnectionPoolCall(url));
        if (null != pool) {
            healIfNeed(pool, url);
        } else {
            logger.error("[NOTIFYME] bug detected! pool here must not be null!");
        }
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#create(com.alipay.remoting.Url)
     */
    @Override
    public Connection create(Url url) throws RemotingException {
        Connection conn = null;
        try {
            conn = this.connectionFactory.createConnection(url);
        } catch (Exception e) {
            throw new RemotingException("Create connection failed. The address is "
                    + url.getOriginUrl(), e);
        }
        return conn;
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#create(java.lang.String, int, int)
     */
    @Override
    public Connection create(String ip, int port, int connectTimeout) throws RemotingException {
        Connection conn = null;
        try {
            conn = this.connectionFactory.createConnection(ip, port, connectTimeout);
        } catch (Exception e) {
            throw new RemotingException("Create connection failed. The address is " + ip + ":"
                    + port, e);
        }
        return conn;
    }

    /**
     * @see com.alipay.remoting.ConnectionManager#create(java.lang.String, int)
     */
    @Override
    public Connection create(String address, int connectTimeout) throws RemotingException {
        Url url = this.addressParser.parse(address);
        url.setConnectTimeout(connectTimeout);
        return create(url);
    }

    /**
     * @see com.alipay.remoting.ConnectionHeartbeatManager#disableHeartbeat(com.alipay.remoting.Connection)
     */
    @Override
    public void disableHeartbeat(Connection connection) {
        if (null != connection) {
            connection.getChannel().attr(Connection.HEARTBEAT_SWITCH).set(false);
        }
    }

    /**
     * @see com.alipay.remoting.ConnectionHeartbeatManager#enableHeartbeat(com.alipay.remoting.Connection)
     */
    @Override
    public void enableHeartbeat(Connection connection) {
        if (null != connection) {
            connection.getChannel().attr(Connection.HEARTBEAT_SWITCH).set(true);
        }
    }

    // ~~~ private methods

    /**
     * get connection pool from future task
     *
     * @param task
     * @return
     */
    private ConnectionPool getConnectionPool(RunStateRecordedFutureTask<ConnectionPool> task) {
        return FutureTaskUtil.getFutureTaskResult(task, logger);

    }

    /**
     * Get the mapping instance of {@link ConnectionPool} with the specified poolKey,
     * or create one if there is none mapping in connTasks.
     *
     * @param poolKey  mapping key of {@link ConnectionPool}
     * @param callable the callable task
     * @return a non-nullable instance of {@link ConnectionPool}
     * @throws RemotingException    if there is no way to get an available {@link ConnectionPool}
     * @throws InterruptedException
     */
    private ConnectionPool getConnectionPoolAndCreateIfAbsent(String poolKey, Callable<ConnectionPool> callable)
            throws RemotingException,InterruptedException {

        RunStateRecordedFutureTask<ConnectionPool> initialTask;
        ConnectionPool pool = null;

        int retry = DEFAULT_RETRY_TIMES;  //2

        int timesOfResultNull = 0;
        int timesOfInterrupt = 0;

        for (int i = 0; (i < retry) && (pool == null); ++i) {
            // 1. 根据 poolKey 从 connTasks 获取 RunStateRecordedFutureTask 实例
            initialTask = this.connTasks.get(poolKey);
            // 2. 如果为 null，创建一个 RunStateRecordedFutureTask 实例，并设置 {poolKey, RunStateRecordedFutureTask 实例} 到 connTasks 中
            if (null == initialTask) {

                // 包装 callable
                initialTask = new RunStateRecordedFutureTask<ConnectionPool>(callable);
                //下一次就直接从该缓存取出任务 initialTask（之后直接从 task 中取出 ConnectionPool），不再 new
                initialTask = this.connTasks.putIfAbsent(poolKey, initialTask);
                System.out.println(Thread.currentThread()+"========第一次取initialTask============"+initialTask);
                // 注意：这里为什么做二次判断？
                // 在高并发的情况下，有可能同一个 poolKey 下的两个 RpcClient 同时走到这里（我们无法预判用户会怎样使用 Bolt），那么在 putIfAbsent 的时候只有一个可以成功（否则就会创建双倍的预期连接数），
                // 则先成功的返回 null，后成功的返回旧值，也就是前边插入的 initialTask 实例，一定不为 null
                if (null == initialTask) {
                    initialTask = this.connTasks.get(poolKey);
                    //ConnectionPoolCall.call()方法
                    // 3. 直接运行 RunStateRecordedFutureTask 实例
                    initialTask.run();
                }
            }

            try {
                //阻塞的获取连接池
                // 从 RunStateRecordedFutureTask 实例中获取 ConnectionPool
                pool = initialTask.get();
                if (null == pool) {
                    if (i + 1 < retry) {
                        timesOfResultNull++;
                        continue;
                    }
                    this.connTasks.remove(poolKey);
                    String errMsg = "Get future task result null for poolKey [" + poolKey + "] after [" + (timesOfResultNull + 1) + "] times try.";
                    throw new RemotingException(errMsg);
                }
            } catch (InterruptedException e) {
                if (i + 1 < retry) {
                    timesOfInterrupt++;
                    continue;// retry if interrupted
                }
                this.connTasks.remove(poolKey);
                logger.warn("Future task of poolKey {} interrupted {} times. InterruptedException thrown and stop retry.",
                                poolKey, (timesOfInterrupt + 1), e);
                throw e;
            } catch (ExecutionException e) {
                // DO NOT retry if ExecutionException occurred
                this.connTasks.remove(poolKey);

                Throwable cause = e.getCause();
                if (cause instanceof RemotingException) {
                    throw (RemotingException) cause;
                } else {
                    FutureTaskUtil.launderThrowable(cause);
                }
            }
        }
        return pool;
    }

    /**
     * remove task and remove all connections
     *
     * @param poolKey
     */
    private void removeTask(String poolKey) {
        RunStateRecordedFutureTask<ConnectionPool> task = this.connTasks.remove(poolKey);
        if (null != task) {
            ConnectionPool pool = FutureTaskUtil.getFutureTaskResult(task, logger);
            if (null != pool) {
                pool.removeAllAndTryClose();
            }
        }
    }

    /**
     * execute heal connection tasks if the actual number of connections in pool is less than expected
     *
     * @param pool
     * @param url
     */
    private void healIfNeed(ConnectionPool pool, Url url) throws RemotingException,
            InterruptedException {
        String poolKey = url.getUniqueKey();
        // 同步创建的连接在创建时一定是成功的，否则抛出异常；一旦连接失效，不再重连？
        // only when async creating connections done
        // and the actual size of connections less than expected, the healing task can be run.
        if (pool.isAsyncCreationDone() && pool.size() < url.getConnNum()) {
            FutureTask<Integer> task = this.healTasks.get(poolKey);
            // 仅仅用于防并发，因为在 task 执行一次之后，就会从 healTasks 移除
            if (null == task) {
                task = new FutureTask<Integer>(new HealConnectionCall(url, pool));
                task = this.healTasks.putIfAbsent(poolKey, task);
                if (null == task) {
                    task = this.healTasks.get(poolKey);
                    task.run();
                }
            }
            try {
                int numAfterHeal = task.get();
                if (logger.isDebugEnabled()) {
                    logger.debug("[NOTIFYME] - conn num after heal {}, expected {}, warmup {}",
                            numAfterHeal, url.getConnNum(), url.isConnWarmup());
                }
            } catch (InterruptedException e) {
                this.healTasks.remove(poolKey);
                throw e;
            } catch (ExecutionException e) {
                // heal task is one-off 一次性的, remove from cache directly after run
                this.healTasks.remove(poolKey);
                Throwable cause = e.getCause();
                if (cause instanceof RemotingException) {
                    throw (RemotingException) cause;
                } else {
                    FutureTaskUtil.launderThrowable(cause);
                }
            }
            // heal task is one-off, remove from cache directly after run
            this.healTasks.remove(poolKey);
        }
    }

    /**
     * do create connections
     *
     * @param url
     * @param pool
     * @param taskName
     * @param syncCreateNumWhenNotWarmup you can specify this param to ensure at least desired number of connections available in sync way
     *  指定了同步创建的个数，默认为1，即需要同步创建一个 Connection，其他的都异步创建
     * @throws RemotingException
     */

    private void doCreate(final Url url, final ConnectionPool pool, final String taskName,
                          final int syncCreateNumWhenNotWarmup) throws RemotingException {
        //创建 poolKey 的 Connection 并添加到 ConnectionPool 中
        final int actualNum = pool.size(); // 池中已有连接数
        final int expectNum = url.getConnNum();   // 期盼总共的连接数
        if (actualNum < expectNum) {
            if (logger.isDebugEnabled()) {
                logger.debug("actual num {}, expect num {}, task name {}", actualNum, expectNum,
                        taskName);
            }
            // 是否配置了连接预热（即需要同步创建好所有的 Connection）
            if (url.isConnWarmup()) {
                for (int i = actualNum; i < expectNum; ++i) {
                    //创建connection
                    // 创建 Connection
                    Connection connection = create(url);
                    // 将 Connection 塞入 ConnectionPool
                    pool.add(connection);
                }
                // 没有配置连接预热，默认同步创建一个 Connection，剩余的 Connection 异步创建
                //todo  这里也有异步创建连接的情况
            } else {
                // 同步创建 Connection，syncCreateNumWhenNotWarmup 指定了同步创建的个数
                if (syncCreateNumWhenNotWarmup < 0 || syncCreateNumWhenNotWarmup > url.getConnNum()) {
                    throw new IllegalArgumentException(
                            "sync create number when not warmup should be [0," + url.getConnNum() + "]");
                }
                // create connection in sync way
                if (syncCreateNumWhenNotWarmup > 0) {
                    for (int i = 0; i < syncCreateNumWhenNotWarmup; ++i) {

                        //创建连接
                        Connection connection = create(url);
                        pool.add(connection);
                    }
                    if (syncCreateNumWhenNotWarmup == url.getConnNum()) {
                        return;
                    }
                }
                // 创建异步创建 Connection 的连接池
                // initialize executor in lazy way
                initializeExecutor();
                pool.markAsyncCreationStart();// mark the start of async
                try {
                    this.asyncCreateConnectionExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                for (int i = pool.size(); i < url.getConnNum(); ++i) {
                                    Connection conn = null;
                                    try {
                                        conn = create(url);
                                    } catch (RemotingException e) {
                                        logger
                                                .error(
                                                        "Exception occurred in async create connection thread for {}, taskName {}",
                                                        url.getUniqueKey(), taskName, e);
                                    }
                                    pool.add(conn);
                                }
                            } finally {
                                pool.markAsyncCreationDone();// mark the end of async
                            }
                        }
                    });
                } catch (RejectedExecutionException e) {
                    pool.markAsyncCreationDone();// mark the end of async when reject
                    throw e;
                }
            } // end of NOT warm up
        } // end of if
    }

    /**
     * initialize executor
     */
    private void initializeExecutor() {
        if (!this.executorInitialized) {
            this.executorInitialized = true;
            this.asyncCreateConnectionExecutor = new ThreadPoolExecutor(minPoolSize, maxPoolSize,
                    keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize),
                    new NamedThreadFactory("Bolt-conn-warmup-executor", true));
        }
    }

    /**
     * Getter method for property <tt>connectionSelectStrategy</tt>.
     *
     * @return property value of connectionSelectStrategy
     */
    public ConnectionSelectStrategy getConnectionSelectStrategy() {
        return connectionSelectStrategy;
    }

    /**
     * Setter method for property <tt>connectionSelectStrategy</tt>.
     *
     * @param connectionSelectStrategy value to be assigned to property connectionSelectStrategy
     */
    public void setConnectionSelectStrategy(ConnectionSelectStrategy connectionSelectStrategy) {
        this.connectionSelectStrategy = connectionSelectStrategy;
    }

    // ~~~ getters and setters

    /**
     * Getter method for property <tt>addressParser</tt>.
     *
     * @return property value of addressParser
     */
    public RemotingAddressParser getAddressParser() {
        return addressParser;
    }

    /**
     * Setter method for property <tt>addressParser</tt>.
     *
     * @param addressParser value to be assigned to property addressParser
     */
    public void setAddressParser(RemotingAddressParser addressParser) {
        this.addressParser = addressParser;
    }

    /**
     * Getter method for property <tt>connctionFactory</tt>.
     *
     * @return property value of connctionFactory
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Setter method for property <tt>connctionFactory</tt>.
     *
     * @param connectionFactory value to be assigned to property connctionFactory
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Getter method for property <tt>connectionEventHandler</tt>.
     *
     * @return property value of connectionEventHandler
     */
    public ConnectionEventHandler getConnectionEventHandler() {
        return connectionEventHandler;
    }

    /**
     * Setter method for property <tt>connectionEventHandler</tt>.
     *
     * @param connectionEventHandler value to be assigned to property connectionEventHandler
     */
    public void setConnectionEventHandler(ConnectionEventHandler connectionEventHandler) {
        this.connectionEventHandler = connectionEventHandler;
    }

    /**
     * Getter method for property <tt>connectionEventListener</tt>.
     *
     * @return property value of connectionEventListener
     */
    public ConnectionEventListener getConnectionEventListener() {
        return connectionEventListener;
    }

    /**
     * Setter method for property <tt>connectionEventListener</tt>.
     *
     * @param connectionEventListener value to be assigned to property connectionEventListener
     */
    public void setConnectionEventListener(ConnectionEventListener connectionEventListener) {
        this.connectionEventListener = connectionEventListener;
    }

    /**
     * Getter method for property <tt>connPools</tt>.
     *
     * @return property value of connPools
     */
    public ConcurrentHashMap<String, RunStateRecordedFutureTask<ConnectionPool>> getConnPools() {
        return this.connTasks;
    }

    /**
     * a callable definition for initialize {@link ConnectionPool}
     *
     * @author tsui
     * @version $Id: ConnectionPoolCall.java, v 0.1 Mar 8, 2016 10:43:51 AM xiaomin.cxm Exp $
     */
    private class ConnectionPoolCall implements Callable<ConnectionPool> {
        private boolean whetherInitConnection;
        private Url url;

        /**
         * create a {@link ConnectionPool} but not init connections
         */
        public ConnectionPoolCall() {
            this.whetherInitConnection = false;
        }

        /**
         * create a {@link ConnectionPool} and init connections with the specified {@link Url}
         *
         * @param url
         */
        public ConnectionPoolCall(Url url) {
            this.whetherInitConnection = true;
            this.url = url;
        }

        @Override
        public ConnectionPool call() throws Exception {
            //不明白这里为什么这么使用 也不是线程啊
            System.out.println(Thread.currentThread()+"===获取或者创建 poolKey 的 ConnectionPool==============");
            //获取或者创建 poolKey 的 ConnectionPool
            final ConnectionPool pool = new ConnectionPool(connectionSelectStrategy);

            if (whetherInitConnection) {
                try {
                    //创建 poolKey 的 Connection 并添加到 ConnectionPool 中
                    doCreate(this.url, pool, this.getClass().getSimpleName(), 1);
                } catch (Exception e) {
                    pool.removeAllAndTryClose();
                    throw e;
                }
            }
            // 返回 ConnectionPool
            return pool;
        }

    }

    /**
     * a callable definition for healing connections in {@link ConnectionPool}
     *
     * @author tsui
     * @version $Id: HealConnectionCall.java, v 0.1 Jul 20, 2017 10:23:23 AM xiaomin.cxm Exp $
     */
    private class HealConnectionCall implements Callable<Integer> {
        private Url url;
        private ConnectionPool pool;

        /**
         * create a {@link ConnectionPool} and init connections with the specified {@link Url}
         *
         * @param url
         */
        public HealConnectionCall(Url url, ConnectionPool pool) {
            this.url = url;
            this.pool = pool;
        }

        @Override
        public Integer call() throws Exception {
            // 创建连接（与建连一样，不再分析）
            doCreate(this.url, this.pool, this.getClass().getSimpleName(), 0);
            // 返回连接池中的连接数量
            return this.pool.size();
        }
    }
}
