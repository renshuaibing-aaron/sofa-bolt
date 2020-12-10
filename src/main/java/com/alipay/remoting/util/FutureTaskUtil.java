package com.alipay.remoting.util;

import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;

/**
 * Utils for future task
 *
 * @author tsui
 * @version $Id: FutureTaskUtil.java, v 0.1 2017-07-24 17:07 tsui Exp $
 */
public class FutureTaskUtil {
    /**
     * get the result of a future task
     * <p>
     * Notice: the run method of this task should have been called at first.
     *
     * @param task
     * @param <T>
     * @return
     */
    public static <T> T getFutureTaskResult(RunStateRecordedFutureTask<T> task, Logger logger) {
        T t = null;
        if (null != task) {
            try {
                t = task.getAfterRun();
            } catch (InterruptedException e) {
                logger.error("Future task interrupted!", e);
            } catch (ExecutionException e) {
                logger.error("Future task execute failed!", e);
            } catch (FutureTaskNotRunYetException e) {
                logger.error("Future task has not run yet!", e);
            }
        }
        return t;
    }

    /**
     * launder the throwable
     *
     * @param t
     */
    public static void launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new IllegalStateException("Not unchecked!", t);
        }
    }
}