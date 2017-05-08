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
import ch.cyberduck.core.threading.BackgroundActionListener;
import ch.cyberduck.core.threading.MainAction;

import java.util.concurrent.Future;

public interface Controller extends ProgressListener, TranscriptListener, BackgroundActionListener {

    /**
     * Will queue up the <code>BackgroundAction</code> to be run in a background thread. Will be executed
     * as soon as no other previous <code>BackgroundAction</code> is pending.
     * Will return immediatly but not run the runnable before the lock of the runnable is acquired.
     *
     * @param runnable The runnable to execute in a secondary Thread
     * @return Future result
     * @see java.lang.Thread
     */
    <T> Future<T> background(BackgroundAction<T> runnable);

    /**
     * Run on main thread. Caller thread is blocked until the selector on the main thread is called.
     *
     * @param runnable The action to execute
     * @see #invoke(ch.cyberduck.core.threading.MainAction, boolean)
     */
    void invoke(MainAction runnable);

    /**
     * Run on main thread.
     *
     * @param runnable The action to execute
     * @param wait     Block calling thread
     */
    void invoke(MainAction runnable, boolean wait);
}
