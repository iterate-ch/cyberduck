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

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class ExecutorServiceThreadPool implements ThreadPool {
    private static final Logger log = Logger.getLogger(ExecutorServiceThreadPool.class);

    private final AbstractExecutorService pool;

    public ExecutorServiceThreadPool(final AbstractExecutorService pool) {
        this.pool = pool;
    }

    @Override
    public void shutdown(boolean gracefully) {
        if(gracefully) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Shutdown pool %s gracefully", pool));
            }
            pool.shutdown();
            try {
                while(!pool.awaitTermination(1L, TimeUnit.SECONDS)) {
                    log.warn(String.format("Await termination for pool %s", pool));
                }
            }
            catch(InterruptedException e) {
                log.error(String.format("Failure awaiting pool termination. %s", e.getMessage()));
            }
        }
        else {
            if(log.isInfoEnabled()) {
                log.info(String.format("Shutdown pool %s now", pool));
            }
            pool.shutdownNow();
        }
    }

    /**
     * @param command Action to run in its own executor thread
     * @return Future result
     */
    @Override
    public <T> Future<T> execute(final Callable<T> command) {
        return pool.submit(command);
    }

    @Override
    public AbstractExecutorService executor() {
        return pool;
    }
}
