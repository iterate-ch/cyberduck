package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

public class DefaultBackgroundExecutor implements BackgroundExecutor {
    private static final Logger log = LogManager.getLogger(DefaultBackgroundExecutor.class);

    private static final DefaultBackgroundExecutor DEFAULT = new DefaultBackgroundExecutor();

    public static BackgroundExecutor get() {
        return DEFAULT;
    }

    private final ThreadPool concurrentExecutor;

    public DefaultBackgroundExecutor() {
        this(new LoggingUncaughtExceptionHandler());
    }

    public DefaultBackgroundExecutor(final Thread.UncaughtExceptionHandler handler) {
        this(ThreadPool.DEFAULT_THREAD_NAME_PREFIX, PreferencesFactory.get().getInteger("threading.pool.size.max"), handler);
    }

    public DefaultBackgroundExecutor(final String prefix) {
        this(prefix, PreferencesFactory.get().getInteger("threading.pool.size.max"), new LoggingUncaughtExceptionHandler());
    }

    public DefaultBackgroundExecutor(final String prefix, final int size) {
        this(prefix, size, new LoggingUncaughtExceptionHandler());
    }

    public DefaultBackgroundExecutor(final String prefix, final int size, final Thread.UncaughtExceptionHandler handler) {
        this(ThreadPoolFactory.get(prefix, size, ThreadPool.Priority.norm, new LinkedBlockingQueue<>(Integer.MAX_VALUE), handler));
    }

    public DefaultBackgroundExecutor(final ThreadPool concurrentExecutor) {
        this.concurrentExecutor = concurrentExecutor;
    }

    @Override
    public <T> Future<T> execute(final Controller controller, final BackgroundActionRegistry registry, final BackgroundAction<T> action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run action %s in background", action));
        }
        // Add action to registry of controller. Will be removed automatically when stopped
        registry.add(action);
        // Start background task
        final BackgroundCallable<T> command = new BackgroundCallable<>(action, controller);
        if(command.init()) {
            try {
                final Future<T> task = concurrentExecutor.execute(command);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Scheduled background runnable %s for execution", action));
                }
                return task;
            }
            catch(RejectedExecutionException e) {
                log.error(String.format("Error scheduling background task %s for execution. %s", action, e.getMessage()));
                action.cancel();
                action.cleanup();
                return ConcurrentUtils.constantFuture(null);
            }
        }
        else {
            return ConcurrentUtils.constantFuture(null);
        }
    }

    @Override
    public void shutdown() {
        if(log.isInfoEnabled()) {
            log.info(String.format("Terminating concurrent executor thread pool %s", concurrentExecutor));
        }
        concurrentExecutor.shutdown(false);
    }
}
