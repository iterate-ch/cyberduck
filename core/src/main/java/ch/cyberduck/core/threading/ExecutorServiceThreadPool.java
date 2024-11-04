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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class ExecutorServiceThreadPool implements ThreadPool {
    private static final Logger log = LogManager.getLogger(ExecutorServiceThreadPool.class);

    private final AbstractExecutorService pool;

    public ExecutorServiceThreadPool(final AbstractExecutorService pool) {
        this.pool = pool;
    }

    @Override
    public void shutdown(boolean gracefully) {
        if(gracefully) {
            log.info("Shutdown pool {} gracefully", pool);
            pool.shutdown();
        }
        else {
            log.info("Shutdown pool {} now", pool);
            pool.shutdownNow();
        }
        try {
            while(!pool.awaitTermination(1L, TimeUnit.SECONDS)) {
                log.warn("Await termination for pool {}", pool);
            }
        }
        catch(InterruptedException e) {
            log.error("Failure awaiting pool termination. {}", e.getMessage());
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
