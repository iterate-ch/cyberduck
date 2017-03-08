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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
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
        super(1 == size ?
                Executors.newSingleThreadExecutor(new NamedThreadFactory(prefix, handler)) :
                new BlockingThreadPoolExecutor(0, size,
                        PreferencesFactory.get().getLong("threading.pool.keepalive.seconds"), TimeUnit.SECONDS,
                        new SynchronousQueue<>(true),
                        new NamedThreadFactory(prefix, handler)));
    }

    private static final class BlockingThreadPoolExecutor extends ThreadPoolExecutor {
        public BlockingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
            this.setRejectedExecutionHandler(new QueuingRejectedExecutionHandler(workQueue));
        }

        private final class QueuingRejectedExecutionHandler implements RejectedExecutionHandler {
            private final BlockingQueue<Runnable> queue;

            public QueuingRejectedExecutionHandler(final BlockingQueue<Runnable> queue) {
                this.queue = queue;
            }

            @Override
            public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
                try {
                    queue.put(r);
                }
                catch(InterruptedException e) {
                    throw new RejectedExecutionException(e);
                }
            }
        }
    }
}