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

import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.ActionOperationBatcherFactory;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.core.threading.ControllerMainAction;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @version $Id$
 */
public abstract class AbstractController implements Controller {
    private static Logger log = Logger.getLogger(AbstractController.class);

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
     * @param runnable The runnable to execute in a secondary Thread
     * @see java.lang.Thread
     * @see ch.cyberduck.core.threading.BackgroundAction#lock()
     */
    @Override
    public <T> Future<T> background(final BackgroundAction<T> runnable) {
        if(log.isDebugEnabled()) {
            log.debug("background:" + runnable);
        }
        runnable.init();
        actions.add(runnable);
        // Start background task
        Callable<T> command = new Callable<T>() {
            @Override
            public T call() {
                // Synchronize all background threads to this lock so actions run
                // sequentially as they were initiated from the main interface thread
                synchronized(runnable.lock()) {
                    final ActionOperationBatcher autorelease = ActionOperationBatcherFactory.get();
                    if(log.isDebugEnabled()) {
                        log.debug("Acquired lock for background runnable:" + runnable);
                    }
                    try {
                        if(runnable.prepare()) {
                            // Execute the action of the runnable
                            return runnable.call();
                        }
                    }
                    catch(Exception e) {
                        log.error("Exception running background task:" + e.getMessage(), e);
                    }
                    finally {
                        // Increase the run counter
                        runnable.finish();
                        // Invoke the cleanup on the main thread to let the action synchronize the user interface
                        invoke(new ControllerMainAction(AbstractController.this) {
                            @Override
                            public void run() {
                                try {
                                    runnable.cleanup();
                                }
                                catch(Exception e) {
                                    log.error("Exception running cleanup task:" + e.getMessage(), e);
                                }
                            }
                        });
                        if(log.isDebugEnabled()) {
                            log.debug("Releasing lock for background runnable:" + runnable);
                        }
                        autorelease.operate();
                    }
                }
                // Canceled action yields no result
                return null;
            }
        };
        final Future<T> future = ThreadPool.instance().execute(command);
        if(log.isInfoEnabled()) {
            log.info(String.format("Scheduled background runnable %s for execution", runnable));
        }
        return future;
    }

    private static ScheduledExecutorService timerPool;

    public static ScheduledExecutorService getTimerPool() {
        if(null == timerPool) {
            timerPool = Executors.newScheduledThreadPool(1);
        }
        return timerPool;
    }
}
