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

import java.util.Set;

public interface Cache<T extends Referenceable> {
    /**
     * @param parent Directory
     * @return True if directory is cached
     */
    boolean isCached(T parent);

    /**
     * @return True if no directory is cached
     */
    boolean isEmpty();

    boolean isHidden(T item);

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
     * Remove from cache
     *
     * @param parent Directory
     * @return Previous list
     */
    AttributedList<T> remove(T parent);

    /**
     * @return Set of folders that have a cached directory listing
     */
    Set<T> keySet();

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

    T lookup(CacheReference<T> reference);
}
