package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Collection;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class BackgroundActionRegistry extends Collection<BackgroundAction> implements BackgroundActionListener {
    private static final Logger log = Logger.getLogger(BackgroundActionRegistry.class);

    private static final long serialVersionUID = 1721336643608575003L;

    private static BackgroundActionRegistry global = null;

    private static final Object lock = new Object();

    public static BackgroundActionRegistry global() {
        synchronized(lock) {
            if(null == global) {
                global = new BackgroundActionRegistry();
            }
            return global;
        }
    }

    private final Set<BackgroundAction> running = new LinkedHashSet<>();

    public BackgroundActionRegistry() {
        //
    }

    private final Object identity = new Object();

    /**
     * @return The currently running background action. Null if none is currently running.
     */
    public synchronized BackgroundAction getCurrent() {
        final Iterator<BackgroundAction> iter = running.iterator();
        if(iter.hasNext()) {
            return iter.next();
        }
        return null;
    }

    @Override
    public synchronized void start(final BackgroundAction action) {
        running.add(action);
    }

    @Override
    public synchronized void stop(final BackgroundAction action) {
        running.remove(action);
    }

    @Override
    public synchronized void cancel(final BackgroundAction action) {
        if(action.isRunning()) {
            log.debug(String.format("Skip removing action %s currently running", action));
        }
        else {
            this.remove(action);
        }
    }

    /**
     * Actions added are automatically removed when canceled or stopped.
     *
     * @param action Action to run in background
     * @return True
     */
    @Override
    public synchronized boolean add(final BackgroundAction action) {
        action.addListener(this);
        return super.add(action);
    }

    @Override
    public synchronized boolean remove(final Object action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Remove action %s", action));
        }
        if(super.remove(action)) {
            ((BackgroundAction) action).removeListener(this);
        }
        return true;
    }


    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        if(!super.equals(o)) {
            return false;
        }
        final BackgroundActionRegistry that = (BackgroundActionRegistry) o;
        return Objects.equals(identity, that.identity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), identity);
    }
}
