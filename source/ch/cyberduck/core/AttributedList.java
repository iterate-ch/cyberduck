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
public class AttributedList extends ArrayList {

    //primary attributes
    protected static final String FILTER = "FILTER";
    protected static final String COMPARATOR = "COMPARATOR";

    protected static final String EXPANDED = "EXPANDED";
    protected static final String HIDDEN = "HIDDEN";

    //file listing has changed
    protected static final String INVALID = "INVALID";


    private Attributes attributes;

    public AttributedList() {
        this.attributes = new Attributes();
    }

    /**
     * Initialize an attributed list with default attributes
     *
     * @param List
     */
    public AttributedList(List List) {
        super(List);
        this.attributes = new Attributes();
    }

    public AttributedList(Attributes attributes) {
        this.attributes = attributes;
    }

    public AttributedList(List List, Attributes attributes) {
        super(List);
        this.attributes = attributes;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Container for file listing attributes, such as a sorting comparator and filter
     *
     * @see Filter
     * @see BrowserComparator
     */
    protected class Attributes extends HashMap {
        /**
         * Initialize with default values
         */
        public Attributes() {
            this.put(FILTER, new NullFilter());
            this.put(COMPARATOR, new NullComparator());
            this.put(HIDDEN, new HashSet());
            this.put(EXPANDED, new Boolean(false));
            this.put(INVALID, new Boolean(false));
        }

        public Attributes(Comparator comparator, Filter filter) {
            this.put(COMPARATOR, comparator);
            this.put(FILTER, filter);
            this.put(HIDDEN, new HashSet());
            this.put(EXPANDED, new Boolean(false));
            this.put(INVALID, new Boolean(false));
        }

        public void setExpanded(boolean expanded) {
            this.put(EXPANDED, new Boolean(expanded));
        }

        public boolean isExpanded() {
            return this.get(EXPANDED).equals(Boolean.TRUE);
        }

        public void addHidden(Path child) {
            ((Set) this.get(HIDDEN)).add(child);
        }

    }
}
