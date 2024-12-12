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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultThreadPool extends ExecutorServiceThreadPool {

    public DefaultThreadPool() {
        this(PreferencesFactory.get().getInteger("threading.pool.size.max"));
    }

    /**
     * New thread pool with first-in-first-out ordered fair wait queue.
     *
     * @param size Number of concurrent threads
     */
    public DefaultThreadPool(final int size) {
        this(DEFAULT_THREAD_NAME_PREFIX, size);
    }

    /**
     * New thread pool with first-in-first-out ordered fair wait queue and unlimited number of threads.
     *
     * @param prefix Thread name prefix
     */
    public DefaultThreadPool(final String prefix) {
        this(prefix, PreferencesFactory.get().getInteger("threading.pool.size.max"));
    }

    /**
     * New thread pool with first-in-first-out ordered fair wait queue.
     *
     * @param prefix Thread name prefix
     * @param size   Maximum number of threads in pool
     */
    public DefaultThreadPool(final String prefix, final int size) {
        this(prefix, size, new LoggingUncaughtExceptionHandler());
    }

    /**
     * New thread pool with first-in-first-out ordered fair wait queue.
     *
     * @param size    Maximum number of threads in pool
     * @param handler Uncaught thread exception handler
     */
    public DefaultThreadPool(final int size, final Thread.UncaughtExceptionHandler handler) {
        this(DEFAULT_THREAD_NAME_PREFIX, size, handler);
    }

    public DefaultThreadPool(final String prefix, final int size, final Thread.UncaughtExceptionHandler handler) {
        this(prefix, size, Priority.norm, handler);
    }

    public DefaultThreadPool(final String prefix, final int size, final Priority priority, final Thread.UncaughtExceptionHandler handler) {
        this(prefix, size, priority, new LinkedBlockingQueue<>(size), new CustomCallerPolicy(), handler);
    }

    public DefaultThreadPool(final String prefix, final int size, final Priority priority, final BlockingQueue<Runnable> queue,
                             final Thread.UncaughtExceptionHandler handler) {
        this(prefix, size, priority, queue, new CustomCallerPolicy(), handler);
    }

    public DefaultThreadPool(final String prefix, final int size, final Priority priority, final BlockingQueue<Runnable> queue,
                             final RejectedExecutionHandler policy, final Thread.UncaughtExceptionHandler handler) {
        super(createExecutor(prefix, size, priority, queue, policy, handler));
    }

    public static ThreadPoolExecutor createExecutor(final String prefix, final int size, final Priority priority,
                                                    final BlockingQueue<Runnable> queue,
                                                    final RejectedExecutionHandler policy,
                                                    final Thread.UncaughtExceptionHandler handler) {
        return new ThreadPoolExecutor(size, size,
                PreferencesFactory.get().getLong("threading.pool.keepalive.seconds"), TimeUnit.SECONDS,
                queue, new NamedThreadFactory(prefix, priority, handler), policy) {
            @Override
            protected void afterExecute(final Runnable r, final Throwable t) {
                if(t != null) {
                    handler.uncaughtException(Thread.currentThread(), t);
                }
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("ThreadPoolExecutor{");
                sb.append("prefix='").append(prefix).append('\'');
                sb.append(", size=").append(size);
                sb.append(", priority=").append(priority);
                sb.append('}');
                return sb.toString();
            }
        };
    }

    public static final class CustomCallerPolicy extends ThreadPoolExecutor.AbortPolicy {
        private static final Logger log = LogManager.getLogger(CustomCallerPolicy.class);

        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
            if(!e.isShutdown()) {
                log.warn("Run {} on caller thread", r);
                r.run();
            }
            else {
                log.error("Rejected execution of {}", r);
                // Reject
                super.rejectedExecution(r, e);
            }
        }
    }
}
