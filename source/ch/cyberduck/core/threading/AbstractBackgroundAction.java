package ch.cyberduck.core.threading;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Id$
 */
public abstract class AbstractBackgroundAction<T> implements BackgroundAction<T> {
    private static final Logger log = Logger.getLogger(AbstractBackgroundAction.class);

    @Override
    public void init() {
        //
    }

    private boolean canceled;

    @Override
    public void cancel() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Cancel background task %s", this));
        }
        canceled = true;
        BackgroundActionListener[] l = listeners.toArray(
                new BackgroundActionListener[listeners.size()]);
        for(BackgroundActionListener listener : l) {
            listener.cancel(this);
        }
    }

    /**
     * To be overriden by a concrete subclass. Returns false by default for actions
     * not connected to a graphical user interface
     *
     * @return True if the user canceled this action
     */
    @Override
    public boolean isCanceled() {
        return canceled;
    }

    private boolean running;

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean prepare() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Prepare background task %s", this));
        }
        if(this.isCanceled()) {
            throw new ConnectionCanceledException();
        }
        running = true;
        BackgroundActionListener[] l = listeners.toArray(
                new BackgroundActionListener[listeners.size()]);
        for(BackgroundActionListener listener : l) {
            listener.start(this);
        }
        return true;
    }

    @Override
    public void finish() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Finish background task %s", this));
        }
        running = false;
        BackgroundActionListener[] l = listeners.toArray(
                new BackgroundActionListener[listeners.size()]);
        for(BackgroundActionListener listener : l) {
            listener.stop(this);
        }
    }

    @Override
    public T call() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run background task %s", this));
        }
        this.run();
        return null;
    }

    @Override
    public void cleanup() {
        //
    }

    protected String toString(List<Path> files) {
        StringBuilder name = new StringBuilder();
        name.append(files.get(0).getName());
        if(files.size() > 1) {
            name.append("…");
        }
        return name.toString();
    }

    private Set<BackgroundActionListener> listeners
            = Collections.synchronizedSet(new HashSet<BackgroundActionListener>());

    @Override
    public void addListener(BackgroundActionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(BackgroundActionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getActivity() {
        return Locale.localizedString("Unknown");
    }

    @Override
    public String toString() {
        return this.getActivity();
    }

    private final Object lock = new Object();

    @Override
    public Object lock() {
        // No synchronization with other tasks by default
        return lock;
    }
}