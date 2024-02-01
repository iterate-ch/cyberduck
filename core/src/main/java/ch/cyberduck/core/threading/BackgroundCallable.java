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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public class BackgroundCallable<T> implements Callable<T> {
    private static final Logger log = LogManager.getLogger(BackgroundCallable.class);

    private final BackgroundAction<T> action;
    private final Controller controller;

    /**
     * Keep client stacktrace
     */
    private final Exception client = new Exception();

    public BackgroundCallable(final BackgroundAction<T> action, final Controller controller) {
        this.action = action;
        this.controller = controller;
    }

    public boolean init() {
        try {
            action.init();
            return true;
        }
        catch(BackgroundException e) {
            action.alert(e);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Invoke cleanup for background action %s", action));
            }
            // Invoke the cleanup on the main thread to let the action synchronize the user interface
            controller.invoke(new ControllerMainAction(controller) {
                @Override
                public void run() {
                    try {
                        action.cleanup();
                    }
                    catch(Exception e) {
                        log.error(String.format("Exception %s running cleanup task", e), e);
                    }
                }
            });
            return false;
        }
    }

    @Override
    public T call() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Running background action %s", action));
        }
        final ActionOperationBatcher autorelease = ActionOperationBatcherFactory.get();
        if(action.isCanceled()) {
            // Canceled action yields no result
            return null;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Prepare background action %s", action));
        }
        action.prepare();
        try {
            final T result = this.run();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return result %s from background action %s", result, action));
            }
            return result;
        }
        finally {
            action.finish();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Invoke cleanup for background action %s", action));
            }
            // Invoke the cleanup on the main thread to let the action synchronize the user interface
            controller.invoke(new ControllerMainAction(controller) {
                @Override
                public void run() {
                    try {
                        action.cleanup();
                    }
                    catch(Exception e) {
                        log.error(String.format("Exception %s running cleanup task", e), e);
                    }
                }
            });
            if(log.isDebugEnabled()) {
                log.debug(String.format("Releasing lock for background runnable %s", action));
            }
            autorelease.operate();
        }
    }

    protected T run() {
        try {
            // Execute the action of the runnable
            if(log.isDebugEnabled()) {
                log.debug(String.format("Call background action %s", action));
            }
            return action.call();
        }
        catch(BackgroundException e) {
            this.failure(client, e);
            // If there was any failure, display the summary now
            if(action.alert(e)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Retry background action %s", action));
                }
                // Retry
                return this.run();
            }
            // Failed action yields no result
            return null;
        }
        catch(Exception e) {
            this.failure(client, e);
            // Runtime failure
            if(action.alert(new DefaultExceptionMappingService().map(e))) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Retry background action %s", action));
                }
                // Retry
                return this.run();
            }
            // Failed action yields no result
            return null;
        }
    }

    protected void failure(final Exception trace, final Exception failure) {
        try {
            trace.initCause(failure);
        }
        catch(IllegalStateException e) {
            log.warn(String.format("Failure overwriting cause for failure %s with %s", trace, failure));
        }
        if(failure instanceof ConnectionCanceledException) {
            log.debug(String.format("Canceled running background task %s", action), trace);
        }
        else if(failure instanceof UnsupportedException) {
            log.debug(String.format("Unsupported running background task %s", action), trace);
        }
        else {
            log.warn(String.format("Failure running background task %s", action), trace);
        }
    }
}
