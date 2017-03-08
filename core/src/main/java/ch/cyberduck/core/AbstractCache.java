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
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractCache<T extends Referenceable> implements Cache<T> {
    private static final Logger log = Logger.getLogger(AbstractCache.class);

    private final Map<T, AttributedList<T>> impl;

    private final Map<CacheReference, T> reverse;

    public AbstractCache(int size) {
        if(size == Integer.MAX_VALUE) {
            // Unlimited
            impl = Collections.synchronizedMap(new LinkedHashMap<T, AttributedList<T>>());
            reverse = Collections.synchronizedMap(new LinkedHashMap<CacheReference, T>());
        }
        else if(size == 0) {
            impl = Collections.emptyMap();
            reverse = Collections.emptyMap();
        }
        else {
            // Will inflate to the given size
            impl = Collections.synchronizedMap(new LRUMap<T, AttributedList<T>>(size));
            reverse = Collections.synchronizedMap(new LinkedHashMap<CacheReference, T>());
        }
    }

    protected abstract CacheReference key(final T object);

    /**
     * Lookup a path by reference in the cache.
     *
     * @param reference A child object of a cached directory listing in the cache
     * @return Null if the path is not in the cache
     * @see ch.cyberduck.core.AttributedList#get(Referenceable)
     */
    public T lookup(final CacheReference reference) {
        final T parent = reverse.get(reference);
        final AttributedList<T> list = impl.get(parent);
        if(null == list) {
            log.warn(String.format("Lookup failed for %s in reverse cache", reference));
            return null;
        }
        final T[] entries = (T[]) list.toArray(new Referenceable[list.size()]);
        for(T entry : entries) {
            if(this.key(entry).equals(reference)) {
                return entry;
            }
        }
        final List<T> hidden = list.attributes().getHidden();
        for(T entry : hidden) {
            if(this.key(entry).equals(reference)) {
                return entry;
            }
        }
        log.warn(String.format("Lookup failed for %s in reverse cache", reference));
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
        for(T r : removed) {
            reverse.remove(this.key(r));
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
            log.warn(String.format("No cache for %s", reference));
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
        if(log.isInfoEnabled()) {
            log.info(String.format("Caching %s", reference));
        }
        for(T f : children) {
            final CacheReference key = this.key(f);
            reverse.remove(key);
            reverse.put(key, reference);
        }
        for(T f : children.attributes().getHidden()) {
            final CacheReference key = this.key(f);
            reverse.remove(key);
            reverse.put(key, reference);
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
            log.warn(String.format("No cache for %s", reference));
        }
    }

    /**
     * Clear all cached directory listings
     */
    public void clear() {
        if(log.isInfoEnabled()) {
            log.info(String.format("Clearing cache %s", this.toString()));
        }
        impl.clear();
        reverse.clear();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Cache{");
        sb.append("size=").append(impl.size());
        sb.append('}');
        return sb.toString();
    }
}