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

import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A sortable list with a map to lookup values by key.
 *
 * @version $Id$
 */
public class AttributedList<E extends AbstractPath> extends CopyOnWriteArrayList<E> {
    protected static Logger log = Logger.getLogger(Cache.class);

    /**
     * The filter to apply to the directory listing
     * excluding files from display.
     */
    protected static final String FILTER = "FILTER";

    /**
     * Sort the file listing using this comparator.
     */
    protected static final String COMPARATOR = "COMPARATOR";

    /**
     * Hidden attribute holds a list of hidden files.
     */
    protected static final String HIDDEN = "HIDDEN";

    /**
     * The cached version should be superseded
     * with an updated listing.
     */
    private static final String INVALID = "INVALID";

    /**
     * file listing is not readable; permission issue
     */
    private static final String READABLE = "READABLE";

    /**
     * Metadata of file listing
     */
    private Attributes<E> attributes
            = new Attributes<E>();

    /**
     * Initialize an attributed list with default attributes
     */
    public AttributedList() {
        this(Collections.<E>emptyList());
    }

    /**
     * @param collection
     */
    public AttributedList(java.util.Collection<E> collection) {
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
         *
         * @see ch.cyberduck.core.NullComparator
         * @see ch.cyberduck.core.NullPathFilter
         */
        public Attributes() {
            this(new NullComparator<E>(), new NullPathFilter());
        }

        /**
         * @param comparator
         * @param filter
         */
        public Attributes(Comparator<E> comparator, PathFilter filter) {
            this.put(COMPARATOR, comparator);
            this.put(FILTER, filter);
            this.put(HIDDEN, new ArrayList());
            this.put(INVALID, Boolean.FALSE);
            this.put(READABLE, Boolean.TRUE);
        }

        /**
         * @param child
         */
        public void addHidden(E child) {
            ((List<E>) this.get(HIDDEN)).add(child);
        }

        /**
         * @return
         */
        public List<E> getHidden() {
            return (List<E>) this.get(HIDDEN);
        }

        public void setReadable(boolean readable) {
            this.put(READABLE, readable);
        }

        /**
         * @return True if the readable attribute is set to <code>Boolean.TRUE</code>.
         */
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
                // Reset readable attribute.
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

    /**
     * Additional key,value table to lookup paths by reference
     */
    private Map<PathReference, E> references
            = new ConcurrentHashMap<PathReference, E>();

    @Override
    public boolean add(E path) {
        final AbstractPath previous = references.put(path.getReference(), path);
        if(null != previous) {
            log.warn("Replacing " + previous + " with " + path + " in file listing.");
        }
        return super.add(path);
    }

    @Override
    public boolean addAll(java.util.Collection<? extends E> c) {
        for(E path : c) {
            final AbstractPath previous = references.put(path.getReference(), path);
            if(null != previous) {
                log.warn("Replacing " + previous + " with " + path + " in file listing.");
            }
        }
        return super.addAll(c);
    }

    /**
     * @param reference
     * @return
     */
    public E get(PathReference reference) {
        return references.get(reference);
    }

    public boolean contains(PathReference reference) {
        return references.containsKey(reference);
    }

    /**
     * The CopyOnWriteArrayList iterator does not support remove but the sort implementation
     * makes use of it. Provide our own implementation here to circumvent.
     *
     * @param comparator
     * @see java.util.Collections#sort(java.util.List, java.util.Comparator)
     * @see java.util.concurrent.CopyOnWriteArrayList#iterator()
     */
    public void sort(Comparator comparator) {
        // Because AttributedList is a CopyOnWriteArrayList we cannot use Collections#sort
        AbstractPath[] sorted = this.toArray(new AbstractPath[this.size()]);
        Arrays.sort(sorted, (Comparator<AbstractPath>) comparator);
        for(int j = 0; j < sorted.length; j++) {
            this.set(j, (E) sorted[j]);
        }
    }

    @Override
    public void clear() {
        attributes.clear();
        references.clear();
        super.clear();
    }
}