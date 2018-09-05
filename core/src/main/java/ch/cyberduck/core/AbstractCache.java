package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractCache<T extends Referenceable> implements Cache<T> {
    private static final Logger log = Logger.getLogger(AbstractCache.class);

    private final Map<T, AttributedList<T>> impl;

    public AbstractCache(int size) {
        if(size == Integer.MAX_VALUE) {
            // Unlimited
            impl = Collections.synchronizedMap(new LinkedHashMap<T, AttributedList<T>>());
        }
        else if(size == 0) {
            impl = Collections.emptyMap();
        }
        else {
            // Will inflate to the given size
            impl = Collections.synchronizedMap(new LRUMap<T, AttributedList<T>>(size));
        }
    }

    @Override
    public T lookup(final CacheReference<T> reference) {
        return null;
    }

    public boolean isEmpty() {
        return impl.isEmpty();
    }

    public Set<T> keySet() {
        return impl.keySet();
    }

    /**
     * @param reference Absolute path
     * @return True if the directory listing of this path is cached
     */
    public boolean containsKey(final T reference) {
        return impl.containsKey(reference);
    }

    /**
     * Remove the cached directory listing for this path
     *
     * @param reference Reference to the path in cache.
     * @return The previously cached directory listing
     */
    public AttributedList<T> remove(final T reference) {
        final AttributedList<T> removed = impl.remove(reference);
        if(null == removed) {
            // Not previously in cache
            return AttributedList.emptyList();
        }
        return removed;
    }

    /**
     * @param reference Absolute path
     * @return An empty list if no cached file listing is available
     * @throws java.util.ConcurrentModificationException If the caller is iterating of the cache himself
     *                                                   and requests a new filter here.
     */
    public AttributedList<T> get(final T reference) {
        AttributedList<T> children = impl.get(reference);
        if(null == children) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("No cache for %s", reference));
            }
            return AttributedList.emptyList();
        }
        return children;
    }

    /**
     * @param reference Reference to the path in cache.
     * @param children  Cached directory listing
     * @return Previous cached version
     */
    public AttributedList<T> put(final T reference, final AttributedList<T> children) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Caching %s", reference));
        }
        return impl.put(reference, children);
    }

    /**
     * @return True if this path denotes a directory and its file listing is cached for this session
     */
    public boolean isCached(final T reference) {
        return this.containsKey(reference);
    }

    public boolean isValid(final T reference) {
        if(this.isCached(reference)) {
            return !this.get(reference).attributes().isInvalid();
        }
        return false;
    }

    /**
     * @param reference Path reference
     */
    public void invalidate(final T reference) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Invalidate %s", reference));
        }
        if(this.containsKey(reference)) {
            this.get(reference).attributes().setInvalid(true);
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug(String.format("No cache for %s", reference));
            }
        }
    }

    /**
     * Clear all cached directory listings
     */
    public void clear() {
        if(log.isInfoEnabled()) {
            log.info(String.format("Clear cache %s", this));
        }
        impl.clear();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Cache{");
        sb.append("size=").append(impl.size());
        sb.append('}');
        return sb.toString();
    }
}
