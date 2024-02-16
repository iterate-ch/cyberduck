package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadPool {
    private static final Logger log = LogManager.getLogger(ScheduledThreadPool.class);

    private final ScheduledExecutorService pool;

    /**
     * With FIFO (first-in-first-out) ordered wait queue.
     */
    public ScheduledThreadPool() {
        this(new LoggingUncaughtExceptionHandler());
    }

    /**
     * With FIFO (first-in-first-out) ordered wait queue.
     *
     * @param handler Uncaught exception handler
     */
    public ScheduledThreadPool(final Thread.UncaughtExceptionHandler handler) {
        this(handler, "timer");
    }

    public ScheduledThreadPool(final String threadNamePrefix) {
        this(new LoggingUncaughtExceptionHandler(), threadNamePrefix);
    }

    public ScheduledThreadPool(final Thread.UncaughtExceptionHandler handler, final String threadNamePrefix) {
        this.pool = Executors.newScheduledThreadPool(1, new NamedThreadFactory(threadNamePrefix, handler));
    }

    /**
     * Schedule at fixed rate with no delay
     *
     * @param runnable Task to run
     * @param period   Repeat after
     * @param unit     Unit for period
     * @return Scheduled future
     */
    public ScheduledFuture<?> repeat(final Runnable runnable, final Long period, final TimeUnit unit) {
        return this.repeat(runnable, 0L, period, unit);
    }

    /**
     * Schedule at fixed rate with delay
     *
     * @param runnable Task to run
     * @param delay    Delay prior starting at fixed rate
     * @param period   Repeat after
     * @param unit     Unit for period
     * @return Scheduled future
     */
    public ScheduledFuture repeat(final Runnable runnable, final long delay, final Long period, final TimeUnit unit) {
        return pool.scheduleAtFixedRate(runnable, delay, period, unit);
    }

    /**
     * Schedule for single execution
     *
     * @param runnable Task to run
     * @param delay    Delay prior running
     * @param unit     Unit for delay
     * @return Scheduled future
     */
    public ScheduledFuture<?> schedule(final Runnable runnable, final Long delay, final TimeUnit unit) {
        return pool.schedule(runnable, delay, unit);
    }

    public void shutdown() {
        if(log.isInfoEnabled()) {
            log.info(String.format("Shutdown pool %s", pool));
        }
        pool.shutdown();
    }
}
