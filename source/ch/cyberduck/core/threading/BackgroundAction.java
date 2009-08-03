package ch.cyberduck.core.threading;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

/**
 * @version $Id$
 */
public interface BackgroundAction extends Runnable {

    /**
     * Called before synchronized with other pending actions
     */
    abstract void init();

    /**
     * Called just before #run.
     *
     * @see #run()
     * @return True if proceed to runnning
     */
    abstract boolean prepare();

    /**
     * Called form a worker thread not blocking the user interface
     */
    abstract void run();

    /**
     * Called after #run but still on the working thread
     *
     * @see #run
     */
    abstract void finish();

    abstract boolean isRunning();

    /**
     * To be called from the main interface thread after the #run
     * has finished to allow calls to non-threadable view classes
     */
    abstract void cleanup();

    /**
     * Mark this action as canceled. Will not execute if scheduled.
     */
    abstract void cancel();

    abstract boolean isCanceled();

    /**
     * @return The name of the activity to display in the activity window
     */
    abstract String getActivity();

    /**
     * The synchronization object
     *
     * @return
     */
    abstract Object lock();

    /**
     * @param listener A listener to be notified
     * @see ch.cyberduck.core.threading.BackgroundActionListener
     */
    abstract void addListener(BackgroundActionListener listener);

    abstract void removeListener(BackgroundActionListener listener);
}
