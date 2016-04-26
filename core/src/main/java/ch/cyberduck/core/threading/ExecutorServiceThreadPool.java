package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class ExecutorServiceThreadPool implements ThreadPool {
    private static final Logger log = Logger.getLogger(ExecutorServiceThreadPool.class);

    private final ExecutorService pool;

    public ExecutorServiceThreadPool(final ExecutorService pool) {
        this.pool = pool;
    }

    @Override
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

    @Override
    public void await(long timeout, TimeUnit unit) {
        try {
            pool.awaitTermination(timeout, unit);
        }
        catch(InterruptedException e) {
            log.warn(e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        this.shutdown(true);
    }

    /**
     * @param command Action to run in its own executor thread
     */
    @Override
    public void execute(final Runnable command) {
        pool.execute(command);
    }

    /**
     * @param command Action to run in its own executor thread
     * @return Future result
     */
    @Override
    public <T> Future<T> execute(final Callable<T> command) throws RejectedExecutionException {
        return pool.submit(command);
    }
}
