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

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public class ThreadPool {
    private static final Logger log = Logger.getLogger(ThreadPool.class);

    private final ExecutorService pool;

    /**
     * With FIFO (first-in-first-out) ordered wait queue.
     */
    public ThreadPool() {
        pool = Executors.newSingleThreadExecutor(new NamedThreadFactory("background"));
    }

    public ThreadPool(final Thread.UncaughtExceptionHandler handler) {
        pool = Executors.newCachedThreadPool(new NamedThreadFactory("background", handler));
    }

    /**
     * With FIFO (first-in-first-out) ordered wait queue.
     *
     * @param size Number of concurrent threads
     */
    public ThreadPool(final int size) {
        this(size, "background");
    }

    public ThreadPool(final String prefix) {
        pool = Executors.newCachedThreadPool(new NamedThreadFactory(prefix));
    }

    public ThreadPool(final int size, final String prefix) {
        pool = Executors.newFixedThreadPool(size, new NamedThreadFactory(prefix));
    }

    public ThreadPool(final int size, final Thread.UncaughtExceptionHandler handler) {
        pool = Executors.newFixedThreadPool(size, new NamedThreadFactory("background", handler));
    }

    public void shutdown(boolean gracefully) {
        if(gracefully) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Shutdown pool %s gracefully", pool));
            }
            pool.shutdown();
        }
        else {
            if(log.isInfoEnabled()) {
                log.info(String.format("Shutdown pool %s now", pool));
            }
            pool.shutdownNow();
        }
    }

    public void await(long timeout, TimeUnit unit) {
        try {
            pool.awaitTermination(timeout, unit);
        }
        catch(InterruptedException e) {
            log.warn(e.getMessage());
        }
    }

    public void shutdown() {
        this.shutdown(true);
    }

    /**
     * @param command Action to run in its own executor thread
     */
    public void execute(final Runnable command) {
        pool.execute(command);
    }

    /**
     * @param command Action to run in its own executor thread
     * @return Future result
     */
    public <T> Future<T> execute(final Callable<T> command) throws RejectedExecutionException {
        return pool.submit(command);
    }
}