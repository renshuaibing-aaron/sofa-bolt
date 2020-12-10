package com.alipay.remoting;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.TimeUnit;

/**
 * A singleton holder of the timer for timeout.
 *
 * @author jiangping
 * @version $Id: TimerHolder.java, v 0.1 2015-09-28 2:02:20 tao Exp $
 */
public class TimerHolder {

    private final static long defaultTickDuration = 10;

    private TimerHolder() {
    }

    /**
     * Get a singleton instance of {@link Timer}. <br>
     * The tick duration is {@link #defaultTickDuration}.
     *
     * @return Timer
     */
    public static Timer getTimer() {
        return DefaultInstance.INSTANCE;
    }

    private static class DefaultInstance {
        static final Timer INSTANCE = new HashedWheelTimer(new NamedThreadFactory(
                "DefaultTimer" + defaultTickDuration, true),
                defaultTickDuration, TimeUnit.MILLISECONDS);
    }
}