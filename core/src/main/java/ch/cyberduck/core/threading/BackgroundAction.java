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

public interface BackgroundAction<T> extends BackgroundActionState {

    /**
     * Called before synchronized with other pending actions
     */
    void init();

    /**
     * Called just before #run.
     *
     * @see #run()
     */
    void prepare();

    T run() throws BackgroundException;

    /**
     * Called form a worker thread not blocking the user interface
     *
     */
    T call() throws BackgroundException;

    /**
     * Called after #run but still on the working thread
     *
     * @see #run
     */
    void finish();

    /**
     * To be called from the main interface thread after the #run
     * has finished to allow calls to non-threadable view classes
     */
    void cleanup();

    /**
     * Mark this action as canceled. Will not execute if scheduled.
     */
    void cancel();

    /**
     * @return The name of the activity to display in the activity window
     */
    String getActivity();

    String getName();

    /**
     * @param listener A listener to be notified
     * @see ch.cyberduck.core.threading.BackgroundActionListener
     */
    void addListener(BackgroundActionListener listener);

    void removeListener(BackgroundActionListener listener);

    /**
     * @param e Connection failure
     * @return True to retry
     */
    boolean alert(BackgroundException e);
}
