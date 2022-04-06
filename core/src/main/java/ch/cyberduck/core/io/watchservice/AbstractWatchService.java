package ch.cyberduck.core.io.watchservice;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.WatchKey;
import java.nio.file.Watchable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Base implementation class for watch services.
 */

public abstract class AbstractWatchService implements RegisterWatchService {

    // signaled keys waiting to be dequeued
    private final LinkedBlockingDeque<WatchKey> pendingKeys =
            new LinkedBlockingDeque<>();

    // special key to indicate that watch service is closed
    private final WatchKey CLOSE_KEY = new AbstractWatchKey(null) {
        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void cancel() {
        }

        @Override
        public Watchable watchable() {
            return null;
        }
    };

    // used when closing watch service
    private volatile boolean closed;
    private final Object closeLock = new Object();

    protected AbstractWatchService() {
    }

    // used by AbstractWatchKey to enqueue key

    final void enqueueKey(WatchKey key) {
        pendingKeys.offer(key);
    }

    /**
     * Throws ClosedWatchServiceException if watch service is closed
     */
    private void checkOpen() {
        if(closed) {
            throw new ClosedWatchServiceException();
        }
    }

    /**
     * Checks the key isn't the special CLOSE_KEY used to unblock threads when the watch service is closed.
     */
    private void checkKey(WatchKey key) {
        if(CLOSE_KEY.equals(key)) {
            // re-queue in case there are other threads blocked in take/poll
            enqueueKey(key);
        }
        checkOpen();
    }

    @Override
    public final WatchKey poll() {
        checkOpen();
        WatchKey key = pendingKeys.poll();
        checkKey(key);
        return key;
    }

    @Override
    public final WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        checkOpen();
        WatchKey key = pendingKeys.poll(timeout, unit);
        checkKey(key);
        return key;
    }

    @Override
    public final WatchKey take() throws InterruptedException {
        checkOpen();
        WatchKey key = pendingKeys.take();
        checkKey(key);
        return key;
    }

    /**
     * Tells whether or not this watch service is open.
     */
    final boolean isOpen() {
        return !closed;
    }

    /**
     * Retrieves the object upon which the close method synchronizes.
     */
    final Object closeLock() {
        return closeLock;
    }

    @Override
    public final void close() throws IOException {
        synchronized(closeLock) {
            // nothing to do if already closed
            if(closed) {
                return;
            }
            closed = true;

            release();

            // clear pending keys and queue special key to ensure that any
            // threads blocked in take/poll wakeup
            pendingKeys.clear();
            pendingKeys.offer(CLOSE_KEY);
        }
    }
}
