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
import java.util.Collection;

/**
 * @version $Id$
 */
public class AttributedList<E extends AbstractPath> extends ArrayList<E> {

    //primary attributes
    protected static final String FILTER = "FILTER";
    protected static final String COMPARATOR = "COMPARATOR";

    protected static final String HIDDEN = "HIDDEN";

    /**
     * file listing has changed; the cached version should be superseded
     */
    private static final String INVALID = "INVALID";

    /**
     * file listing is not readable; permission issue
     */
    private static final String READABLE = "READABLE";

    private Attributes<E> attributes;

    /**
     * Initialize an attributed list with default attributes
     */
    public AttributedList() {
        this.attributes = new Attributes<E>();
    }

    public AttributedList(java.util.Collection<E> collection) {
        this.attributes = new Attributes<E>();
        this.addAll(collection);
    }

    private static final AttributedList<AbstractPath> EMPTY_LIST = new EmptyList();

    public static <T extends AbstractPath> AttributedList<T> emptyList() {
        return (AttributedList<T>) EMPTY_LIST;
    }

    private static class EmptyList extends AttributedList<AbstractPath> {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean contains(Object obj) {
            return false;
        }

        @Override
        public AbstractPath get(int index) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        // Preserves singleton property
        private Object readResolve() {
            return EMPTY_LIST;
        }
    }

    /**
     * Container for file listing attributes, such as a sorting comparator and filter
     *
     * @see PathFilter
     * @see BrowserComparator
     */
    public class Attributes<E> extends HashMap<String, Object> {
        /**
         * Initialize with default values
         */
        public Attributes() {
            this.put(FILTER, new NullPathFilter());
            this.put(COMPARATOR, new NullComparator<E>());
            this.put(HIDDEN, new HashSet());
            this.put(INVALID, Boolean.FALSE);
            this.put(READABLE, Boolean.TRUE);
        }

        public Attributes(Comparator<E> comparator, PathFilter filter) {
            this.put(COMPARATOR, comparator);
            this.put(FILTER, filter);
            this.put(HIDDEN, new HashSet());
            this.put(INVALID, Boolean.FALSE);
            this.put(READABLE, Boolean.TRUE);
        }

        public void addHidden(E child) {
            ((Set<E>) this.get(HIDDEN)).add(child);
        }

        public Set<E> getHidden() {
            return (Set<E>) this.get(HIDDEN);
        }

        public void setReadable(boolean readable) {
            this.put(READABLE, readable);
        }

        public boolean isReadable() {
            return this.get(READABLE).equals(Boolean.TRUE);
        }

        /**
         * Mark cached listing as superseded
         *
         * @param dirty
         */
        public void setDirty(boolean dirty) {
            this.put(INVALID, dirty);
            if(dirty) {
                this.put(READABLE, Boolean.TRUE);
            }
        }

        /**
         * @return true if the listing should be superseded
         */
        public boolean isDirty() {
            return this.get(INVALID).equals(Boolean.TRUE);
        }
    }

    public Attributes<E> attributes() {
        return attributes;
    }

    private Map<PathReference, E> references = new HashMap<PathReference, E>();

    @Override
    public boolean add(E path) {
        references.put(path.getReference(), path);
        return super.add(path);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        for(E item : c) {
            references.put(item.getReference(), item);
        }
        return super.addAll(c);
    }

    public E get(PathReference reference) {
        return references.get(reference);
    }

    @Override
    public void clear() {
        attributes.clear();
        references.clear();
        super.clear();
    }
}