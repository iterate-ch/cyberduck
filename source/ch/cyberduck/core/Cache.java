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

    private AttributedList get(Path path) {
        return (AttributedList)super.get(path);
    }

    public AttributedList get(Path path, Comparator comparator, Filter filter) {
        AttributedList childs = (AttributedList)super.get(path);
        if(!childs.getAttributes().get(Attributes.COMPARATOR).equals(comparator)) {
            Collections.sort(childs, comparator);
            childs.attributes.put(Attributes.COMPARATOR, comparator);
        }
        if(!childs.getAttributes().get(Attributes.FILTER).equals(filter)) {
            childs.addAll((Set)childs.getAttributes().get(Attributes.HIDDEN));
            ((Set)childs.getAttributes().get(Attributes.HIDDEN)).clear();
            for(Iterator i = childs.iterator(); i.hasNext(); ) {
                Path child = (Path)i.next();
                if(!filter.accept(child)) {
                    childs.attributes.addHidden(child);
                    i.remove();
                }
            }
            childs.attributes.put(Attributes.FILTER, filter);
        }
        return childs;
    }

    public Object put(Path path, List childs) {
		return super.put(path, new AttributedList(childs));
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

    public void setExpanded(Path path, boolean expanded) {
        if(path.attributes.isDirectory()) {
            if(this.containsKey(path)) {
                this.get(path).attributes.setExpanded(expanded);
            }
        }
    }

    public boolean isExpanded(Path path) {
        if(path.attributes.isDirectory()) {
            if(this.containsKey(path)) {
                return this.get(path).attributes.isExpanded();
            }
        }
        return false;
    }
}