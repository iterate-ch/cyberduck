package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.util.Map;

public interface Cache<T extends Referenceable> {

    /**
     * @param object Value object
     * @return Key used for internal comparison in map
     */
    CacheReference<T> reference(T object);

    /**
     * @param parent Directory
     * @return True if directory is cached
     */
    boolean isCached(T parent);

    long size();

    /**
     * @return True if no directory is cached
     */
    boolean isEmpty();

    /**
     * @param item Directory
     * @return True if directory is cached and cache is not invalidated
     */
    boolean isValid(T item);

    /**
     * Cache directory listing
     *
     * @param parent   Directory
     * @param children Folder listing
     * @return Return previous list
     */
    AttributedList<T> put(T parent, AttributedList<T> children);

    /**
     * @param parent Directory
     * @return Cached directory listing. Empty if none is cached
     * @see #isCached(Referenceable)
     */
    AttributedList<T> get(T parent);

    /**
     * @return Map representation for cached entries
     */
    Map<CacheReference<T>, AttributedList<T>> asMap();

    /**
     * Remove from cache
     *
     * @param parent Directory
     */
    AttributedList<T> remove(T parent);

    /**
     * Mark cached directory listing as out of date
     *
     * @param parent Directory
     */
    void invalidate(T parent);

    /**
     * Remove all cached directory listing
     */
    void clear();

    /**
     * Acquire exclusive access to look up the contents of a directory. Threads missing the cache at the same time
     * wait for the first to complete the listing instead of listing the same directory concurrently. Reentrant for the
     * owning thread. No exclusive access by default.
     *
     * @param parent Directory
     * @throws ConnectionCanceledException If interrupted while waiting
     * @see #unlock(Referenceable)
     */
    default void lock(T parent) throws ConnectionCanceledException {
        //
    }

    /**
     * Release exclusive access acquired with {@link #lock(Referenceable)} by the current thread
     *
     * @param parent Directory
     */
    default void unlock(T parent) {
        //
    }

    /**
     * @param reference Key for item in cached list
     * @return Cached value for reference
     */
    T lookup(CacheReference<T> reference);
}
