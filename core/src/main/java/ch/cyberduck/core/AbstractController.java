package ch.cyberduck.core;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.core.threading.BackgroundCallable;
import ch.cyberduck.core.threading.DefaultThreadPool;
import ch.cyberduck.core.threading.LoggingUncaughtExceptionHandler;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public abstract class AbstractController implements Controller {
    private static final Logger log = Logger.getLogger(AbstractController.class);

    private final ThreadPool singleExecutor;

    private final ThreadPool concurrentExecutor;

    protected AbstractController() {
        this(new LoggingUncaughtExceptionHandler());
    }

    protected AbstractController(final Thread.UncaughtExceptionHandler handler) {
        singleExecutor = new DefaultThreadPool(1, handler);
        concurrentExecutor = ThreadPoolFactory.get(handler);
    }

    /**
     * Does wait for main action to return before continuing the caller thread.
     *
     * @param runnable The action to execute
     */
    @Override
    public void invoke(final MainAction runnable) {
        this.invoke(runnable, false);
    }

    /**
     * List of pending background tasks or this browser
     */
    private final BackgroundActionRegistry registry
            = new BackgroundActionRegistry();

    /**
     * Pending background actions
     *
     * @return List of tasks.
     */
    public BackgroundActionRegistry getActions() {
        return registry;
    }

    /**
     * @return true if there is any network activity running in the background
     */
    public boolean isActivityRunning() {
        final BackgroundAction current = this.getActions().getCurrent();
        return null != current;
    }

    /**
     * Will queue up the <code>BackgroundAction</code> to be run in a background thread. Will be executed
     * as soon as no other previous <code>BackgroundAction</code> is pending.
     * Will return immediately but not run the runnable before the lock of the runnable is acquired.
     *
     * @param action The runnable to execute in a secondary Thread
     * @see java.lang.Thread
     * @see ch.cyberduck.core.threading.BackgroundAction#lock()
     */
    @Override
    public <T> Future<T> background(final BackgroundAction<T> action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run action %s in background", action));
        }
        if(registry.contains(action)) {
            log.warn(String.format("Skip duplicate background action %s found in registry", action));
            return ConcurrentUtils.constantFuture(null);
        }
        registry.add(action);
        action.init();
        // Start background task
        final Callable<T> command = new BackgroundCallable<T>(action, this, registry);
        try {
            final Future<T> task;
            if(null == action.lock()) {
                task = concurrentExecutor.execute(command);
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Synchronize on lock %s for action %s", action.lock(), action));
                }
                task = singleExecutor.execute(command);
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Scheduled background runnable %s for execution", action));
            }
            return task;
        }
        catch(RejectedExecutionException e) {
            log.error(String.format("Error scheduling background task %s for execution. %s", action, e.getMessage()));
            action.cleanup();
            return ConcurrentUtils.constantFuture(null);
        }
    }

    protected void invalidate() {
        if(log.isInfoEnabled()) {
            log.info(String.format("Terminating single executor thread pool %s", singleExecutor));
        }
        singleExecutor.shutdown(false);
        if(log.isInfoEnabled()) {
            log.info(String.format("Terminating concurrent executor thread pool %s", concurrentExecutor));
        }
        concurrentExecutor.shutdown(false);
    }

    @Override
    public void start(final BackgroundAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Start action %s", action));
        }
    }

    @Override
    public void cancel(final BackgroundAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel action %s", action));
        }
    }

    @Override
    public void stop(final BackgroundAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Stop action %s", action));
        }
    }

    @Override
    public void message(final String message) {
        log.info(message);
    }

    @Override
    public void log(final Type request, final String message) {
        log.trace(message);
    }
}
