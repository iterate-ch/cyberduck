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
    private Map<String, AttributedList<E>> _impl = Collections.<String, AttributedList<E>>synchronizedMap(new LRUMap(
            Preferences.instance().getInteger("browser.cache.size")
    ) {
        @Override
        protected boolean removeLRU(LinkEntry entry) {
            log.debug("Removing from cache:" + entry);
            return true;
        }
    });

    /**
     *
     */
    public Cache() {
        ;
    }

    /**
     * @param path
     * @return
     */
    public E lookup(PathReference path) {
        final AttributedList<E> childs = this.get(Path.getParent(path.toString()));
        if(null == childs) {
            log.warn("Lookup failed for " + path + " in cache");
            return null;
        }
        return childs.get(path);
    }

    /**
     * @param path
     * @return True if the directory listing for this path is cached
     */
    public boolean containsKey(E path) {
        return this.containsKey(path.getAbsolute());
    }

    /**
     * @param path
     * @return
     */
    public boolean containsKey(String path) {
        return _impl.containsKey(path);
    }

    /**
     * Remotes the cached directory listing for this path
     *
     * @param path
     * @return
     */
    public AttributedList<E> remove(E path) {
        return _impl.remove(path.getAbsolute());
    }

    /**
     * Get the childs of this path using the last sorting and filter used
     *
     * @param path
     * @return null if no cached file listing is available
     */
    public AttributedList<E> get(E path) {
        return this.get(path.getAbsolute());
    }

    /**
     * @param path
     * @return
     */
    public AttributedList<E> get(String path) {
        return _impl.get(path);
    }

    /**
     * @param path
     * @param comparator
     * @param filter
     * @return null if no cached file listing is available
     */
    public AttributedList<E> get(final E path, final Comparator<E> comparator, final PathFilter<E> filter) {
        return this.get(path.getAbsolute(), comparator, filter);
    }

    /**
     * @param path
     * @param comparator
     * @param filter
     * @return
     */
    public AttributedList<E> get(final String path, final Comparator<E> comparator, final PathFilter<E> filter) {
        AttributedList<E> childs = _impl.get(path);
        if(null == childs) {
            log.warn("No cache for " + path);
            return new AttributedList<E>();
        }
        boolean needsSorting = !childs.attributes().get(AttributedList.COMPARATOR).equals(comparator);
        boolean needsFiltering = !childs.attributes().get(AttributedList.FILTER).equals(filter);
        if(needsSorting) {
            //do not sort when the list has not been filtered yet
            if(!needsFiltering) {
                Collections.sort(childs, comparator);
            }
            //saving last sorting comparator
            childs.attributes().put(AttributedList.COMPARATOR, comparator);
        }
        if(needsFiltering) {
            //add previously hidden files to childs
            final Set<E> hidden = childs.attributes().getHidden();
            childs.addAll(hidden);
            //clear the previously set of hidden files
            hidden.clear();
            for(Iterator<E> i = childs.iterator(); i.hasNext();) {
                E child = i.next();
                if(!filter.accept(child)) {
                    //child not accepted by filter; add to cached hidden files
                    childs.attributes().addHidden(child);
                    //remove hidden file from current file listing
                    i.remove();
                }
            }
            //saving last filter
            childs.attributes().put(AttributedList.FILTER, filter);
            //sort again because the list has changed
            Collections.sort(childs, comparator);
        }
        return childs;
    }

    public AttributedList<E> put(E path, AttributedList<E> childs) {
        return _impl.put(path.getAbsolute(), childs);
    }

    public void clear() {
        _impl.clear();
    }
}