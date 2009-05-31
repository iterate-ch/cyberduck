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

import java.util.*;

/**
 * A cache for remote directory listings
 * @version $Id$
 */
public class Cache<E extends AbstractPath> {

    /**
     *
     */
    private HashMap<String, AttributedList<E>> _impl = new HashMap<String, AttributedList<E>>();

    /**
     *
     */
    public Cache() {
        ;
    }

    /**
     * @param path
     * @return True if the directory listing for this path is cached
     */
    public boolean containsKey(E path) {
        return this.containsKey(path.getAbsolute());
    }

    /**
     *
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
     *
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
     *
     * @param path
     * @param comparator
     * @param filter
     * @return
     */
    public AttributedList<E> get(final String path, final Comparator<E> comparator, final PathFilter<E> filter) {
        AttributedList<E> childs = _impl.get(path);
        if(null == childs) {
            return null;
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
            childs.addAll((Set) childs.attributes().get(AttributedList.HIDDEN));
            //clear the previously set of hidden files
            ((Set) childs.attributes().get(AttributedList.HIDDEN)).clear();
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