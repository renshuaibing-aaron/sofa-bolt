package com.alipay.remoting;

import com.alipay.remoting.config.ConfigManager;
import com.alipay.remoting.log.BoltLoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.*;

/**
 * Manager of processors<br>
 * Maintains the relationship between command and command processor through command code.
 *ProcessorManager 命令处理器容器的设计：RemotingCommand 处理器的容器
 * @author jiangping
 * @version $Id: ProcessorManager.java, v 0.1 Sept 6, 2015 2:49:47 PM tao Exp $
 */
public class ProcessorManager {
    private static final Logger logger = BoltLoggerFactory
            .getLogger("CommonDefault");

    //这个是什么
    private ConcurrentHashMap<CommandCode, RemotingProcessor<?>> cmd2processors = new ConcurrentHashMap<CommandCode, RemotingProcessor<?>>(
            4);

    private RemotingProcessor<?> defaultProcessor;

    /**
     * The default executor, if no executor is set for processor, this one will be used
     */
    private ExecutorService defaultExecutor;

    private int minPoolSize = ConfigManager
            .default_tp_min_size();

    private int maxPoolSize = ConfigManager
            .default_tp_max_size();

    private int queueSize = ConfigManager
            .default_tp_queue_size();

    private long keepAliveTime = ConfigManager
            .default_tp_keepalive_time();

    public ProcessorManager() {
        defaultExecutor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveTime,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(queueSize), new NamedThreadFactory(
                "Bolt-default-executor", true));
    }

    /**
     * Register processor to process command that has the command code of cmdCode.
     *
     * @param cmdCode
     * @param processor
     */
    public void registerProcessor(CommandCode cmdCode, RemotingProcessor<?> processor) {
        if (this.cmd2processors.containsKey(cmdCode)) {
            logger
                    .warn(
                            "Processor for cmd={} is already registered, the processor is {}, and changed to {}",
                            cmdCode, cmd2processors.get(cmdCode).getClass().getName(), processor.getClass()
                                    .getName());
        }
        this.cmd2processors.put(cmdCode, processor);
    }

    /**
     * Register the default processor to process command with no specific processor registered.
     *
     * @param processor
     */
    public void registerDefaultProcessor(RemotingProcessor<?> processor) {
        if (this.defaultProcessor == null) {
            this.defaultProcessor = processor;
        } else {
            throw new IllegalStateException("The defaultProcessor has already been registered: "
                    + this.defaultProcessor.getClass());
        }
    }

    /**
     * Get the specific processor with command code of cmdCode if registered, otherwise the default processor is returned.
     *
     * @param cmdCode
     * @return
     */
    public RemotingProcessor<?> getProcessor(CommandCode cmdCode) {
        RemotingProcessor<?> processor = this.cmd2processors.get(cmdCode);
        if (processor != null) {
            return processor;
        }
        return this.defaultProcessor;
    }

    /**
     * Getter method for property <tt>defaultExecutor</tt>.
     *
     * @return property value of defaultExecutor
     */
    public ExecutorService getDefaultExecutor() {
        return defaultExecutor;
    }

    /**
     * Set the default executor.
     *
     * @param executor
     */
    public void registerDefaultExecutor(ExecutorService executor) {
        this.defaultExecutor = executor;
    }

}