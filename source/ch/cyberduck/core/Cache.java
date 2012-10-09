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

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Map;

/**
 * A cache for remote directory listings
 *
 * @version $Id$
 */
public class Cache {
    private static final Logger log = Logger.getLogger(Cache.class);

    /**
     *
     */
    private final Map<PathReference, AttributedList<Path>> _impl = Collections.<PathReference, AttributedList<Path>>synchronizedMap(new LRUMap(
            Preferences.instance().getInteger("browser.cache.size")
    ) {
        @Override
        protected boolean removeLRU(LinkEntry entry) {
            log.debug("Removing from cache:" + entry);
            return true;
        }
    });

    /**
     * Lookup a path by reference in the cache. Expensive as its parent directory must be
     * evaluated first.
     *
     * @param reference A child object of a cached directory listing in the cache
     * @return Null if the path is no more cached.
     * @see ch.cyberduck.core.AttributedList#get(PathReference)
     */
    public Path lookup(final PathReference reference) {
        for(AttributedList<Path> list : _impl.values()) {
            final Path path = list.get(reference);
            if(null == path) {
                continue;
            }
            return path;
        }
        log.warn(String.format("Lookup failed for %s in cache", reference));
        return null;
    }

    public boolean isEmpty() {
        return _impl.isEmpty();
    }

    /**
     * @param reference Absolute path
     * @return True if the directory listing of this path is cached
     */
    public boolean containsKey(final PathReference reference) {
        return _impl.containsKey(reference);
    }

    /**
     * Remove the cached directory listing for this path
     *
     * @param reference Reference to the path in cache.
     * @return The previuosly cached directory listing
     */
    public AttributedList<Path> remove(final PathReference reference) {
        return _impl.remove(reference);
    }

    /**
     * @param reference Absolute path
     * @return An empty list if no cached file listing is available
     * @throws ConcurrentModificationException
     *          If the caller is iterating of the cache himself
     *          and requests a new filter here.
     */
    public AttributedList<Path> get(final PathReference reference) {
        AttributedList<Path> children = _impl.get(reference);
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
    public AttributedList<Path> put(final PathReference reference, final AttributedList<Path> children) {
        return _impl.put(reference, children);
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
        this.get(reference).attributes().setInvalid(true);
    }

    /**
     * Clear all cached directory listings
     */
    public void clear() {
        if(log.isInfoEnabled()) {
            log.info(String.format("Clearing cache %s", this.toString()));
        }
        _impl.clear();
    }
}