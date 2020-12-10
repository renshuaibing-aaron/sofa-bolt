package com.alipay.remoting.rpc;

/**
 * Constants for rpc.
 *
 * @author jiangping
 * @version $Id: RpcConfigs.java, v 0.1 2015-10-10 PM3:03:47 tao Exp $
 */
public class RpcConfigs {
    /**
     * Protocol key in url.
     */
    public static final String URL_PROTOCOL = "_PROTOCOL";

    /**
     * Version key in url.
     */
    public static final String URL_VERSION = "_VERSION";

    /**
     * Connection timeout key in url.
     * 连接超时时间
     */
    public static final String CONNECT_TIMEOUT_KEY = "_CONNECTTIMEOUT";

    /**
     * Connection number key of each address
     */
    public static final String CONNECTION_NUM_KEY = "_CONNECTIONNUM";

    /**
     * whether need to warm up connections
     */
    public static final String CONNECTION_WARMUP_KEY = "_CONNECTIONWARMUP";

    /**
     * Whether to dispatch message list in default executor.
     */
    public static final String DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR = "bolt.rpc.dispatch-msg-list-in-default-executor";
    public static final String DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR_DEFAULT = "true";
}