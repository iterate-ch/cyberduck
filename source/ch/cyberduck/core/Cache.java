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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A cache for remote directory listings
 *
 * @version $Id$
 */
public class Cache<T extends Referenceable> {
    private static final Logger log = Logger.getLogger(Cache.class);

    private final Map<PathReference, AttributedList<T>> impl;

    public static <T extends Referenceable> Cache<T> empty() {
        return new Cache<T>(0) {
            @Override
            public AttributedList<T> put(PathReference reference, AttributedList<T> children) {
                return AttributedList.emptyList();
            }
        };
    }

    public Cache() {
        this(PreferencesFactory.get().getInteger("browser.cache.size"));
    }

    public Cache(int size) {
        if(size == Integer.MAX_VALUE) {
            // Unlimited
            impl = new LinkedHashMap<PathReference, AttributedList<T>>();
        }
        else if(size == 0) {
            impl = Collections.emptyMap();
        }
        else {
            // Will inflate to the given size
            impl = Collections.<PathReference, AttributedList<T>>synchronizedMap(new LRUMap(size));
        }
    }

    /**
     * Lookup a path by reference in the cache. Expensive as its parent directory must be
     * evaluated first.
     *
     * @param reference A child object of a cached directory listing in the cache
     * @return Null if the path is not in the cache
     * @see ch.cyberduck.core.AttributedList#get(PathReference)
     */
    public T lookup(final PathReference reference) {
        for(AttributedList<T> list : impl.values().toArray(new AttributedList[impl.size()])) {
            final T path = list.get(reference);
            if(null == path) {
                continue;
            }
            return path;
        }
        log.error(String.format("Lookup failed for %s in cache", reference));
        return null;
    }

    public boolean isEmpty() {
        return impl.isEmpty();
    }

    public Set<PathReference> keySet() {
        return impl.keySet();
    }

    /**
     * @param reference Absolute path
     * @return True if the directory listing of this path is cached
     */
    public boolean containsKey(final PathReference reference) {
        return impl.containsKey(reference);
    }

    /**
     * Remove the cached directory listing for this path
     *
     * @param reference Reference to the path in cache.
     * @return The previously cached directory listing
     */
    public AttributedList<T> remove(final PathReference reference) {
        return impl.remove(reference);
    }

    /**
     * @param reference Absolute path
     * @return An empty list if no cached file listing is available
     * @throws java.util.ConcurrentModificationException If the caller is iterating of the cache himself
     *                                                   and requests a new filter here.
     */
    public AttributedList<T> get(final PathReference reference) {
        AttributedList<T> children = impl.get(reference);
        if(null == children) {
            log.warn(String.format("No cache for %s", reference));
            return AttributedList.emptyList();
        }
        return children;
    }

    public boolean isHidden(final Path file) {
        final List<?> hidden = this.get(file.getParent().getReference()).attributes().getHidden();
        return hidden.contains(file);
    }

    /**
     * @param reference Reference to the path in cache.
     * @param children  Cached directory listing
     * @return Previous cached version
     */
    public AttributedList<T> put(final PathReference reference, final AttributedList<T> children) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Caching %s", reference));
        }
        return impl.put(reference, children);
    }

    /**
     * @return True if this path denotes a directory and its file listing is cached for this session
     * @see ch.cyberduck.core.Cache
     */
    public boolean isCached(final PathReference reference) {
        return this.containsKey(reference) && !this.get(reference).attributes().isInvalid();
    }

    /**
     * @param reference Path reference
     */
    public void invalidate(final PathReference reference) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Invalidate %s", reference));
        }
        if(this.containsKey(reference)) {
            this.get(reference).attributes().setInvalid(true);
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
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Cache{");
        sb.append("size=").append(impl.size());
        sb.append('}');
        return sb.toString();
    }
}