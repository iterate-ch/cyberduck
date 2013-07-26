package ch.cyberduck.ui;

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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.ui.threading.ControllerMainAction;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public abstract class AbstractController implements Controller {
    private static Logger log = Logger.getLogger(AbstractController.class);

    private ThreadPool singleExecutor
            = new ThreadPool();

    private ThreadPool concurrentExecutor
            = new ThreadPool(Integer.MAX_VALUE);

    protected ScheduledExecutorService timerPool
            = Executors.newScheduledThreadPool(1);

    /**
     * Does wait for main action to return before continuing the caller thread.
     *
     * @param runnable The action to execute
     */
    @Override
    public void invoke(MainAction runnable) {
        this.invoke(runnable, true);
    }

    /**
     * List of pending background tasks or this browser
     */
    private BackgroundActionRegistry actions
            = new BackgroundActionRegistry();

    /**
     * Pending background actions
     *
     * @return List of tasks.
     */
    public BackgroundActionRegistry getActions() {
        return actions;
    }

    /**
     * Will queue up the <code>BackgroundAction</code> to be run in a background thread. Will be executed
     * as soon as no other previous <code>BackgroundAction</code> is pending.
     * Will return immediatly but not run the runnable before the lock of the runnable is acquired.
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
        action.init();
        actions.add(action);
        // Start background task
        final Callable<T> command = new BackgroundCallable<T>(action);
        try {
            if(null == action.lock()) {
                return concurrentExecutor.execute(command);
            }
            else {
                return singleExecutor.execute(command);
            }
        }
        catch(RejectedExecutionException e) {
            log.error(String.format("Error scheduling background task %s for execution. %s", action, e.getMessage()));
            action.cleanup();
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Scheduled background runnable %s for execution", action));
        }
        return null;
    }

    @Override
    public ScheduledFuture schedule(final Runnable runnable, final Long period, final TimeUnit unit) {
        return timerPool.scheduleAtFixedRate(runnable, 0L, period, unit);
    }

    public void openUrl(final DescriptiveUrl url) {
        if(url.equals(DescriptiveUrl.EMPTY)) {
            return;
        }
        openUrl(url.getUrl());
    }

    /**
     * Open URL with default web browser.
     *
     * @param url HTTP URL
     */
    public void openUrl(String url) {
        BrowserLauncherFactory.get().open(url);
    }

    protected void invalidate() {
        timerPool.shutdownNow();
        singleExecutor.shutdown();
    }

    private final class BackgroundCallable<T> implements Callable<T> {
        private final BackgroundAction<T> action;

        public BackgroundCallable(final BackgroundAction<T> action) {
            this.action = action;
        }

        @Override
        public T call() {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Synchronize on lock %s for action %s", action.lock(), action));
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Acquired lock for background runnable %s", action));
            }
            try {
                action.prepare();
                // Execute the action of the runnable
                return action.call();
            }
            catch(ConnectionCanceledException e) {
                log.warn(String.format("Connection canceled for background task %s", action));
            }
            catch(BackgroundException e) {
                log.error(String.format("Unhandled exception running background task %s", e.getMessage()), e);
            }
            catch(Exception e) {
                log.fatal(String.format("Unhandled exception running background task %s", e.getMessage()), e);
            }
            finally {
                // Increase the run counter
                try {
                    action.finish();
                }
                finally {
                    actions.remove(action);
                }
                // Invoke the cleanup on the main thread to let the action synchronize the user interface
                invoke(new ControllerMainAction(AbstractController.this) {
                    @Override
                    public void run() {
                        try {
                            action.cleanup();
                        }
                        catch(Exception e) {
                            log.error(String.format("Exception running cleanup task %s", e.getMessage()), e);
                        }
                    }
                });
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Releasing lock for background runnable %s", action));
                }
            }
            // Canceled action yields no result
            return null;
        }
    }
}
