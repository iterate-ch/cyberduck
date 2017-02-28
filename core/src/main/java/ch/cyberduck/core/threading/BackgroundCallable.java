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

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

public class BackgroundCallable<T> implements Callable<T> {
    private static final Logger log = Logger.getLogger(BackgroundCallable.class);

    private final BackgroundAction<T> action;
    private final Controller controller;
    private final BackgroundActionRegistry registry;

    /**
     * Keep client stacktrace
     */
    private final Exception client = new Exception();

    public BackgroundCallable(final BackgroundAction<T> action, final Controller controller, final BackgroundActionRegistry registry) {
        this.action = action;
        this.controller = controller;
        this.registry = registry;
    }

    @Override
    public T call() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Running background action %s", action));
        }
        if(action.isCanceled()) {
            // Canceled action yields no result
            return null;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Prepare background action %s", action));
        }
        action.prepare();
        try {
            return this.run();
        }
        finally {
            try {
                action.finish();
            }
            finally {
                registry.remove(action);
            }
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
                        log.error(String.format("Exception running cleanup task %s", e.getMessage()), e);
                    }
                }
            });
            if(log.isDebugEnabled()) {
                log.debug(String.format("Releasing lock for background runnable %s", action));
            }
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
        catch(ConnectionCanceledException e) {
            // Do not report as failed
            log.warn(String.format("Connection canceled for background task %s", action));
            // Canceled action yields no result
            return null;
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
        log.warn(String.format("Failure running background task %s", failure.getMessage()), trace);
    }
}
