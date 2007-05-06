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
public class Cache {

    private Map _impl = new HashMap();
    /**
     *
     */
    public Cache() {
        ;
    }

    /**
     *
     * @param path
     * @return True if the directory listing for this path is cached
     */
    public boolean containsKey(AbstractPath path) {
        return _impl.containsKey(path.getAbsolute());
    }

    /**
     * Remotes the cached directory listing for this path
     * @param path
     * @return
     */
    public AttributedList remove(AbstractPath path) {
        return (AttributedList)_impl.remove(path.getAbsolute());
    }

    /**
     * Get the childs of this path using the last sorting and filter used
     * @param path
     * @return null if no cached file listing is available
     */
    public AttributedList get(AbstractPath path) {
        return (AttributedList) _impl.get(path.getAbsolute());
    }

    /**
     * @param path
     * @param comparator
     * @param filter
     * @return null if no cached file listing is available
     */
    public AttributedList get(final AbstractPath path, final Comparator comparator, final PathFilter filter) {
        AttributedList childs = (AttributedList) _impl.get(path.getAbsolute());
        if (null == childs) {
            return null;
        }
        boolean needsSorting = !childs.attributes().get(AttributedList.COMPARATOR).equals(comparator);
        boolean needsFiltering = !childs.attributes().get(AttributedList.FILTER).equals(filter);
        if (needsSorting) {
            //do not sort when the list has not been filtered yet
            if (!needsFiltering) {
                Collections.sort(childs, comparator);
            }
            //saving last sorting comparator
            childs.attributes().put(AttributedList.COMPARATOR, comparator);
        }
        if (needsFiltering) {
            //add previously hidden files to childs
            childs.addAll((Set) childs.attributes().get(AttributedList.HIDDEN));
            //clear the previously set of hidden files
            ((Set) childs.attributes().get(AttributedList.HIDDEN)).clear();
            for (Iterator i = childs.iterator(); i.hasNext();) {
                AbstractPath child = (AbstractPath) i.next();
                if (!filter.accept(child)) {
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

    public AttributedList put(AbstractPath path, AttributedList childs) {
        return (AttributedList)_impl.put(path.getAbsolute(), childs);
    }

    public AttributedList[] values() {
        return (AttributedList[])_impl.entrySet().toArray(new AttributedList[]{});
    }

    public AbstractPath[] keys() {
        return (AbstractPath[])_impl.keySet().toArray(new AbstractPath[]{});
    }

    public void clear() {
        _impl.clear();
    }
}