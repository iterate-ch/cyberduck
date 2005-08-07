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

    /**
     *
     * @param path
     * @return null if no cached file listing is available
     */
    public AttributedList get(Path path) {
        return (AttributedList)super.get(path.getAbsolute());
    }

    public boolean containsKey(Path path) {
        return super.containsKey(path.getAbsolute());
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
        if(!childs.getAttributes().get(Attributes.COMPARATOR).equals(comparator)) {
            Collections.sort(childs, comparator);
            //saving last sorting comparator
            childs.attributes.put(Attributes.COMPARATOR, comparator);
        }
        if(!childs.getAttributes().get(Attributes.FILTER).equals(filter)) {
            //add previously hidden files to childs
            childs.addAll((Set)childs.getAttributes().get(Attributes.HIDDEN));
            //clear the previously set of hidden files
            ((Set)childs.getAttributes().get(Attributes.HIDDEN)).clear();
            for(Iterator i = childs.iterator(); i.hasNext(); ) {
                Path child = (Path)i.next();
                if(!filter.accept(child)) {
                    //child not accepted by filter; add to cached hidden files
                    childs.attributes.addHidden(child);
                    //remove hidden file from current file listing
                    i.remove();
                }
            }
            //saving last filter
            childs.attributes.put(Attributes.FILTER, filter);
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

    protected class AttributedList extends ArrayList {

        private Attributes attributes;

        /**
         * Initialize an attributed list with default attributes
         * @param List
         */
        public AttributedList(List List) {
            super(List);
            this.attributes = new Attributes();
        }

        public AttributedList(List List, Attributes attributes) {
            super(List);
            this.attributes = attributes;
        }

        public Attributes getAttributes() {
            return this.attributes;
        }
    }

    /**
     * Container for file listing attributes, such as a sorting comparator and filter
     * @see ch.cyberduck.core.Filter
     * @see ch.cyberduck.core.BrowserComparator
     */
    protected class Attributes extends HashMap {
        //primary attributes
        protected static final String FILTER = "FILTER";
        protected static final String COMPARATOR = "COMPARATOR";

        protected static final String EXPANDED = "EXPANDED";
        protected static final String HIDDEN = "HIDDEN";

        /**
         * Initialize with default values
         */
        public Attributes() {
            this.put(FILTER, new NullFilter());
            this.put(COMPARATOR, new NullComparator());
            this.put(HIDDEN, new HashSet());
            this.put(EXPANDED, new Boolean(false));
        }

        public Attributes(Comparator comparator, Filter filter) {
            this.put(COMPARATOR, comparator);
            this.put(FILTER, filter);
            this.put(HIDDEN, new HashSet());
            this.put(EXPANDED, new Boolean(false));
        }

        public void setExpanded(boolean expanded) {
            this.put(Attributes.EXPANDED, new Boolean(expanded));
        }

        public boolean isExpanded() {
            return this.get(Attributes.EXPANDED).equals(Boolean.TRUE);
        }

        public void addHidden(Path child) {
            ((Set)this.get(HIDDEN)).add(child);
        }

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
                this.get(path).attributes.setExpanded(expanded);
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
                return this.get(path).attributes.isExpanded();
            }
        }
        return false;
    }
}