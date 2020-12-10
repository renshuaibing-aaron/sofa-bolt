package com.alipay.remoting.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A customized FutureTask which can record whether the run method has been called.
 *
 * @author tsui
 * @version $Id: RunStateRecordedFutureTask.java, v 0.1 2017-07-31 16:28 tsui Exp $
 */
public class RunStateRecordedFutureTask<V> extends FutureTask<V> {
    private AtomicBoolean hasRun = new AtomicBoolean();

    public RunStateRecordedFutureTask(Callable<V> callable) {
        super(callable);
    }

    @Override
    public void run() {
        this.hasRun.set(true);
        //ConnectionPoolCall#call 方法
        super.run();
    }

    public V getAfterRun() throws InterruptedException, ExecutionException,
            FutureTaskNotRunYetException {
        if (!hasRun.get()) {
            throw new FutureTaskNotRunYetException();
        }
        return super.get();
    }
}