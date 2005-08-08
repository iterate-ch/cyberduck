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
 * @version $Id$
 */
public class Cache extends HashMap {
	
	private static Map CACHES = new HashMap();

    //Factory constructor
    public static Cache create(String url) {
		if(null == CACHES.get(url)) {
			CACHES.put(url, new Cache());
		}
		return (Cache)CACHES.get(url);
	}
	
	private Cache() {
		//private
	}

    public boolean containsKey(Path path) {
        return super.containsKey(path.getAbsolute());
    }

    public Object remove(Path path) {
        return super.remove(path.getAbsolute());
    }

    public void invalidate(Path path) {
        this.get(path).getAttributes().put(AttributedList.INVALID, new Boolean(true));
    }

    /**
     * @param path
     * @return true if the file listing of the given path has been changed since caching
     */
    public boolean isInvalid(Path path) {
        if(this.exists(path)) {
            return this.get(path).getAttributes().get(AttributedList.INVALID).equals(Boolean.TRUE);
        }
        return false;
    }

    /**
     *
     * @param path
     * @return null if no cached file listing is available
     */
    public AttributedList get(Path path) {
        return (AttributedList)super.get(path.getAbsolute());
    }

    public boolean exists(Path path) {
        return this.get(path) != null;
    }

    /**
     *
     * @param path
     * @param comparator
     * @param filter
     * @return null if no cached file listing is available
     */
    public AttributedList get(Path path, Comparator comparator, Filter filter) {
        AttributedList childs = (AttributedList)super.get(path.getAbsolute());
        if(null == childs) {
            return childs;
        }
        boolean needsSorting = !childs.getAttributes().get(AttributedList.COMPARATOR).equals(comparator);
        boolean needsFiltering = !childs.getAttributes().get(AttributedList.FILTER).equals(filter);
        if(needsSorting) {
            //do not sort when the list has not been filtered yet
            if(!needsFiltering) {
                Collections.sort(childs, comparator);
            }
            //saving last sorting comparator
            childs.getAttributes().put(AttributedList.COMPARATOR, comparator);
        }
        if(needsFiltering) {
            //add previously hidden files to childs
            childs.addAll((Set)childs.getAttributes().get(AttributedList.HIDDEN));
            //clear the previously set of hidden files
            ((Set)childs.getAttributes().get(AttributedList.HIDDEN)).clear();
            for(Iterator i = childs.iterator(); i.hasNext(); ) {
                Path child = (Path)i.next();
                if(!filter.accept(child)) {
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

    public Object put(Path path, List childs) {
		return super.put(path.getAbsolute(), new AttributedList(childs));
	}

    /**
     *
     * @param path ch.cyberduck.core.Path
     * @param childs java.util.List
     */
    public Object put(Object path, Object childs) {
        return this.put((Path)path, (List)childs);
    }

    /**
     * Memorize the given path to be expaned in outline view
     * @param path Must be a directory
     * @param expanded
     * @see ch.cyberduck.core.Attributes#isDirectory()
     */
    public void setExpanded(Path path, boolean expanded) {
        if(path.attributes.isDirectory()) {
            if(this.containsKey(path)) {
                this.get(path).getAttributes().setExpanded(expanded);
            }
        }
    }

    /**
     *
     * @param path Must be a directory
     * @return true if the given path should be expanded in outline view
     * @see ch.cyberduck.core.Attributes#isDirectory()
     */
    public boolean isExpanded(Path path) {
        if(path.attributes.isDirectory()) {
            if(this.containsKey(path)) {
                return this.get(path).getAttributes().isExpanded();
            }
        }
        return false;
    }
}