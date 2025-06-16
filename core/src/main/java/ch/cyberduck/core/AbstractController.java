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
import ch.cyberduck.core.threading.DefaultBackgroundExecutor;
import ch.cyberduck.core.threading.MainAction;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Future;

public abstract class AbstractController implements Controller {
    private static final Logger log = LogManager.getLogger(AbstractController.class);

    /**
     * List of pending background tasks or this browser
     */
    protected final BackgroundActionRegistry registry
            = new BackgroundActionRegistry();

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
     * Pending background actions
     *
     * @return List of tasks.
     */
    @Override
    public BackgroundActionRegistry getRegistry() {
        return registry;
    }

    /**
     * Will queue up the <code>BackgroundAction</code> to be run in a background thread
     *
     * @param action The runnable to execute in a secondary thread
     */
    @Override
    public <T> Future<T> background(final BackgroundAction<T> action) {
        if(registry.contains(action)) {
            log.warn("Skip duplicate background action {} found in registry", action);
            return ConcurrentUtils.constantFuture(null);
        }
        return DefaultBackgroundExecutor.get().execute(this, registry, action);
    }

    /**
     * Free resources and references to model and delegates
     */
    protected void invalidate() {
        // No-op
    }

    @Override
    public void start(final BackgroundAction action) {
        log.debug("Start action {}", action);
    }

    @Override
    public void cancel(final BackgroundAction action) {
        log.debug("Cancel action {}", action);
    }

    @Override
    public void stop(final BackgroundAction action) {
        log.debug("Stop action {}", action);
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
