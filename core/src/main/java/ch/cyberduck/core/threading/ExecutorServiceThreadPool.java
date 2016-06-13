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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ExecutorServiceThreadPool<T> implements ThreadPool<T> {
    private static final Logger log = Logger.getLogger(ExecutorServiceThreadPool.class);

    private final ExecutorService pool;

    private final ExecutorCompletionService<T> completion;

    private final AtomicInteger counter = new AtomicInteger();

    public ExecutorServiceThreadPool(final ExecutorService pool) {
        this.pool = pool;
        this.completion = new ExecutorCompletionService<T>(pool);
    }

    @Override
    public void shutdown(boolean gracefully) {
        if(gracefully) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Shutdown pool %s gracefully", pool));
            }
            try {
                this.await();
            }
            catch(BackgroundException e) {
                log.error(String.format("Ignore failure of executed task in shutdown. %s", e.getMessage()));
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
    public void await() throws BackgroundException {
        for(int i = 0; i < counter.get(); i++) {
            try {
                log.info(String.format("Await completion of task %d of %d", i, counter.get()));
                final Future<T> task = completion.take();
                task.get();
            }
            catch(InterruptedException e) {
                throw new ConnectionCanceledException(e);
            }
            catch(ExecutionException e) {
                log.warn(String.format("Delete failed with execution failure %s", e.getMessage()));
                if(e.getCause() instanceof BackgroundException) {
                    throw (BackgroundException) e.getCause();
                }
                throw new BackgroundException(e);
            }
        }
    }

    /**
     * @param command Action to run in its own executor thread
     * @return Future result
     */
    @Override
    public Future<T> execute(final Callable<T> command) {
        final Future<T> future = completion.submit(command);
        counter.incrementAndGet();
        return future;
    }
}
