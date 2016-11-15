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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.util.concurrent.Callable;

public interface BackgroundAction<T> extends Callable<T> {

    /**
     * Called before synchronized with other pending actions
     */
    void init();

    /**
     * Called just before #run.
     *
     * @see #run()
     */
    void prepare() throws ConnectionCanceledException;

    T run() throws BackgroundException;

    /**
     * Called form a worker thread not blocking the user interface
     */
    @Override
    T call() throws BackgroundException;

    /**
     * Called after #run but still on the working thread
     *
     * @see #run
     */
    void finish();

    boolean isRunning();

    /**
     * To be called from the main interface thread after the #run
     * has finished to allow calls to non-threadable view classes
     */
    void cleanup();

    /**
     * Mark this action as canceled. Will not execute if scheduled.
     */
    void cancel();

    boolean isCanceled();

    /**
     * @return The name of the activity to display in the activity window
     */
    String getActivity();

    String getName();

    /**
     * @return The synchronization object. Null if no ordering is required.
     */
    Object lock();

    /**
     * @param listener A listener to be notified
     * @see ch.cyberduck.core.threading.BackgroundActionListener
     */
    void addListener(BackgroundActionListener listener);

    void removeListener(BackgroundActionListener listener);

    /**
     * @return True to retry
     * @param e Connection failure
     */
    boolean alert(BackgroundException e);
}
