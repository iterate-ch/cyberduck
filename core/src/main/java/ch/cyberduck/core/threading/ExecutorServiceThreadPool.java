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

import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
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

    private final AtomicInteger size = new AtomicInteger();

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
        while(size.get() > 0) {
            final Set<BackgroundException> failures = new HashSet<>();
            try {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Await completion for %d submitted tasks in queue", size.get()));
                }
                final T value = completion.take().get();
                if(log.isInfoEnabled()) {
                    log.info(String.format("Finished task with return value %s", value));
                }
            }
            catch(InterruptedException e) {
                failures.add(new ConnectionCanceledException(e));
            }
            catch(ExecutionException e) {
                log.warn(String.format("Task failed with execution failure %s", e.getMessage()));
                if(e.getCause() instanceof BackgroundException) {
                    failures.add((BackgroundException) e.getCause());
                }
                else {
                    failures.add(new DefaultExceptionMappingService().map(e.getCause()));
                }
            }
            finally {
                size.decrementAndGet();
            }
            if(!failures.isEmpty()) {
                final BackgroundException failure = failures.iterator().next();
                if(failures.size() == 1) {
                    throw failure;
                }
                final StringAppender appender = new StringAppender(System.getProperty("line.separator").charAt(0));
                for(BackgroundException f : failures) {
                    appender.append(StringUtils.capitalize(f.getDetail()));
                }
                failure.setDetail(appender.toString());
                throw failure;
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
        size.incrementAndGet();
        return future;
    }
}
