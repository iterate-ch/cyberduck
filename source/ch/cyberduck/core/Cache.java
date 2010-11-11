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

import java.util.*;

/**
 * A cache for remote directory listings
 *
 * @version $Id$
 */
public class Cache<E extends AbstractPath> {
    protected static Logger log = Logger.getLogger(Cache.class);

    /**
     *
     */
    private Map<PathReference, AttributedList<E>> _impl = Collections.<PathReference, AttributedList<E>>synchronizedMap(new LRUMap(
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
    public E lookup(PathReference reference) {
        for(AttributedList list : _impl.values()) {
            final AbstractPath path = list.get(reference);
            if(null == path) {
                continue;
            }
            return (E) path;
        }
        log.warn("Lookup failed for " + reference + " in cache");
        return null;
    }
    
    public boolean isEmpty() {
        return _impl.isEmpty();
    }

    /**
     * @param reference Absolute path
     * @return True if the directory listing of this path is cached
     */
    public boolean containsKey(PathReference reference) {
        return _impl.containsKey(reference);
    }

    /**
     * Remove the cached directory listing for this path
     *
     * @param reference Reference to the path in cache.
     * @return The previuosly cached directory listing
     */
    public AttributedList<E> remove(PathReference reference) {
        return _impl.remove(reference);
    }

    /**
     * Get the children of this path using the last sorting and filter used
     *
     * @param reference Reference to the path in cache.
     * @return An empty list if no cached file listing is available
     */
    public AttributedList<E> get(PathReference reference) {
        final AttributedList<E> children = this.get(reference, null, null);
        if(null == children) {
            log.warn("No cache for " + reference);
            return AttributedList.emptyList();
        }
        return children;
    }

    /**
     * @param reference       Absolute path
     * @param comparator Sorting comparator to apply the the file listing. If null the list
     *                   is returned as is from the last used comparator
     * @param filter     Path filter to apply. All files that don't match are moved to the
     *                   hidden attribute of the attributed list. If null the list is returned
     *                   with the last filter applied.
     * @return An empty list if no cached file listing is available
     * @throws ConcurrentModificationException
     *          If the caller is iterating of the cache himself
     *          and requests a new filter here.
     */
    public AttributedList<E> get(PathReference reference, Comparator<E> comparator, PathFilter<E> filter) {
        AttributedList<E> children = _impl.get(reference);
        if(null == children) {
            log.warn("No cache for " + reference);
            return AttributedList.emptyList();
        }
        boolean needsSorting = false;
        if(null != comparator) {
            needsSorting = !children.attributes().getComparator().equals(comparator);
        }
        boolean needsFiltering = false;
        if(null != filter) {
            needsFiltering = !children.attributes().getFilter().equals(filter);
        }
        if(needsSorting) {
            // Do not sort when the list has not been filtered yet
            if(!needsFiltering) {
                children.sort(comparator);
            }
            // Saving last sorting comparator
            children.attributes().setComparator(comparator);
        }
        if(needsFiltering) {
            // Add previously hidden files to children
            final List<E> hidden = children.attributes().getHidden();
            children.addAll(hidden);
            // Clear the previously set of hidden files
            hidden.clear();
            for(E child : children) {
                if(!filter.accept(child)) {
                    //child not accepted by filter; add to cached hidden files
                    children.attributes().addHidden(child);
                    //remove hidden file from current file listing
                    children.remove(child);
                }
            }
            // Saving last filter
            children.attributes().setFilter(filter);
            // Sort again because the list has changed
            children.sort(comparator);
        }
        return children;
    }

    /**
     * @param reference Reference to the path in cache.
     * @param children
     * @return
     */
    public AttributedList<E> put(PathReference reference, AttributedList<E> children) {
        return _impl.put(reference, children);
    }

    /**
     * Clear all cached directory listings
     */
    public void clear() {
        log.info("Clearing cache " + this.toString());
        _impl.clear();
    }
}
