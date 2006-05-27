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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @version $Id$
 */
public class Cache extends HashMap {

    public Cache() {
        super();
    }

    public boolean containsKey(Path path) {
        return super.containsKey(path.getAbsolute());
    }

    public Object remove(Path path) {
        return super.remove(path.getAbsolute());
    }

    public void invalidate(Path path) {
        if (this.containsKey(path)) {
            this.get(path).getAttributes().put(AttributedList.INVALID, Boolean.TRUE);
        }
    }

    /**
     * @param path
     * @return true if the file listing of the given path has been changed since caching
     */
    public boolean isInvalid(Path path) {
        return this.get(path).getAttributes().get(AttributedList.INVALID).equals(Boolean.TRUE);
    }

    /**
     * Get the childs of this path using the last sorting and filter used
     * @param path
     * @return null if no cached file listing is available
     */
    private AttributedList get(Path path) {
        return (AttributedList) super.get(path.getAbsolute());
    }

    public Object get(Object key) {
        return this.get((Path)key);
    }

    /**
     * @param path
     * @param comparator
     * @param filter
     * @return null if no cached file listing is available
     */
    public AttributedList get(final Path path, final Comparator comparator, final Filter filter) {
        AttributedList childs = (AttributedList) super.get(path.getAbsolute());
        if (null == childs) {
            return null;
        }
        boolean needsSorting = !childs.getAttributes().get(AttributedList.COMPARATOR).equals(comparator);
        boolean needsFiltering = !childs.getAttributes().get(AttributedList.FILTER).equals(filter);
        if (needsSorting) {
            //do not sort when the list has not been filtered yet
            if (!needsFiltering) {
                Collections.sort(childs, comparator);
            }
            //saving last sorting comparator
            childs.getAttributes().put(AttributedList.COMPARATOR, comparator);
        }
        if (needsFiltering) {
            //add previously hidden files to childs
            childs.addAll((Set) childs.getAttributes().get(AttributedList.HIDDEN));
            //clear the previously set of hidden files
            ((Set) childs.getAttributes().get(AttributedList.HIDDEN)).clear();
            for (Iterator i = childs.iterator(); i.hasNext();) {
                Path child = (Path) i.next();
                if (!filter.accept(child)) {
                    //child not accepted by filter; add to cached hidden files
                    childs.getAttributes().addHidden(child);
                    //remove hidden file from current file listing
                    i.remove();
                }
            }
            //saving last filter
            childs.getAttributes().put(AttributedList.FILTER, filter);
            //sort again because the list has changed
            Collections.sort(childs, comparator);
        }
        return childs;
    }

    public Object put(Path path, AttributedList childs) {
        return super.put(path.getAbsolute(), childs);
    }

    /**
     * @param path   ch.cyberduck.core.Path
     * @param childs ch.cyberduck.core.AttributedList
     */
    public Object put(Object path, Object childs) {
        return this.put((Path) path, (AttributedList) childs);
    }

    public void clear() {
        super.clear();
    }
}