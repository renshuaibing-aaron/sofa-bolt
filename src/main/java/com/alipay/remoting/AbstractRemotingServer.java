package com.alipay.remoting;

import com.alipay.remoting.config.AbstractConfigurableInstance;
import com.alipay.remoting.config.configs.ConfigType;
import com.alipay.remoting.log.BoltLoggerFactory;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Server template for remoting.
 *
 * @author jiangping
 * @version $Id: AbstractRemotingServer.java, v 0.1 2015-9-5 PM7:37:48 tao Exp $
 */
public abstract class AbstractRemotingServer extends AbstractConfigurableInstance implements
        RemotingServer {

    private static final Logger logger = BoltLoggerFactory.getLogger("CommonDefault");

    private AtomicBoolean started = new AtomicBoolean(false);
    private String ip;
    private int port;

    public AbstractRemotingServer(int port) {
        this(new InetSocketAddress(port).getAddress().getHostAddress(), port);
    }

    public AbstractRemotingServer(String ip, int port) {
        super(ConfigType.SERVER_SIDE);
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void init() {
        // Do not call this method, it will be removed in the next version
    }

    @Override
    public boolean start() {
        if (started.compareAndSet(false, true)) {
            try {
                //服务端的初始化
                doInit();

                logger.warn("Prepare to start server on port {} ", port);

                //启动服务端
                if (doStart()) {
                    logger.warn("Server started on port {}", port);
                    return true;
                } else {
                    logger.warn("Failed starting server on port {}", port);
                    return false;
                }
            } catch (Throwable t) {
                this.stop();// do stop to ensure close resources created during doInit()
                throw new IllegalStateException("ERROR: Failed to start the Server!", t);
            }
        } else {
            String errMsg = "ERROR: The server has already started!";
            logger.error(errMsg);
            throw new IllegalStateException(errMsg);
        }
    }

    @Override
    public boolean stop() {
        if (started.compareAndSet(true, false)) {
            return this.doStop();
        } else {
            throw new IllegalStateException("ERROR: The server has already stopped!");
        }
    }

    @Override
    public String ip() {
        return ip;
    }

    @Override
    public int port() {
        return port;
    }

    protected abstract void doInit();

    protected abstract boolean doStart() throws InterruptedException;

    protected abstract boolean doStop();

}
