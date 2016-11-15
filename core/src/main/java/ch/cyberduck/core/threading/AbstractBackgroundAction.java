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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractBackgroundAction<T> implements BackgroundAction<T> {
    private static final Logger log = Logger.getLogger(AbstractBackgroundAction.class);

    private State state;

    protected final Set<BackgroundActionListener> listeners
            = new HashSet<BackgroundActionListener>();

    @Override
    public void init() {
        //
    }

    private enum State {
        running,
        canceled,
        stopped
    }

    @Override
    public void cancel() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel background task %s", this));
        }
        final BackgroundActionListener[] l = listeners.toArray(
                new BackgroundActionListener[listeners.size()]);
        for(BackgroundActionListener listener : l) {
            listener.cancel(this);
        }
        state = State.canceled;
    }

    /**
     * To be overridden by a concrete subclass. Returns false by default for actions
     * not connected to a graphical user interface
     *
     * @return True if the user canceled this action
     */
    @Override
    public boolean isCanceled() {
        return state == State.canceled;
    }

    @Override
    public boolean isRunning() {
        return state == State.running;
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Prepare background task %s", this));
        }
        final BackgroundActionListener[] l = listeners.toArray(
                new BackgroundActionListener[listeners.size()]);
        for(BackgroundActionListener listener : l) {
            listener.start(this);
        }
        state = State.running;
    }

    @Override
    public void finish() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Finish background task %s", this));
        }
        final BackgroundActionListener[] l = listeners.toArray(
                new BackgroundActionListener[listeners.size()]);
        for(BackgroundActionListener listener : l) {
            listener.stop(this);
        }
        state = State.stopped;
    }

    @Override
    public boolean alert(final BackgroundException e) {
        return false;
    }

    @Override
    public T call() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run background task %s", this));
        }
        return this.run();
    }

    @Override
    public void cleanup() {
        //
    }

    protected String toString(final List<Path> files) {
        final StringBuilder name = new StringBuilder();
        name.append(files.get(0).getName());
        if(files.size() > 1) {
            name.append("…");
        }
        return name.toString();
    }

    @Override
    public void addListener(final BackgroundActionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(final BackgroundActionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getActivity() {
        return LocaleFactory.localizedString("Unknown");
    }

    @Override
    public String getName() {
        return LocaleFactory.localizedString("Unknown");
    }

    @Override
    public Object lock() {
        // No synchronization with other tasks by default
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractBackgroundAction{");
        sb.append("state=").append(state);
        sb.append('}');
        return sb.toString();
    }
}