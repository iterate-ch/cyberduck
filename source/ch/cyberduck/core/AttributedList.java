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
    public class Attributes<E> {

        /**
         * Sort the file listing using this comparator.
         */
        private Comparator<E> comparator;

        /**
         * The filter to apply to the directory listing
         * excluding files from display.
         */
        private PathFilter filter;

        /**
         * Hidden attribute holds a list of hidden files.
         */
        private List<E> hidden = new ArrayList<E>();

        /**
         * The cached version should be superseded
         * with an updated listing.
         */
        private boolean invalid = false;

        /**
         * File listing is not readable; permission issue
         */
        private boolean readable = true;

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
            this.comparator = comparator;
            this.filter = filter;
        }

        public Comparator<E> getComparator() {
            return comparator;
        }

        public void setComparator(Comparator<E> comparator) {
            this.comparator = comparator;
        }

        public PathFilter getFilter() {
            return filter;
        }

        public void setFilter(PathFilter filter) {
            this.filter = filter;
        }

        /**
         * @param child
         */
        public void addHidden(E child) {
            hidden.add(child);
        }

        /**
         * @return
         */
        public List<E> getHidden() {
            return hidden;
        }

        public void setReadable(boolean readable) {
            this.readable = readable;
        }

        /**
         * @return True if the readable attribute is set to <code>Boolean.TRUE</code>.
         */
        public boolean isReadable() {
            return readable;
        }

        /**
         * Mark cached listing as superseded
         *
         * @param dirty
         */
        public void setInvalid(boolean dirty) {
            this.invalid = dirty;
            if(dirty) {
                // Reset readable attribute.
                readable = true;
            }
        }

        /**
         * @return true if the listing should be superseded
         */
        public boolean isInvalid() {
            return invalid;
        }
    }

    /**
     * Metadata of the list.
     *
     * @return
     */
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
        if(null == comparator) {
            return;
        }
        // Because AttributedList is a CopyOnWriteArrayList we cannot use Collections#sort
        AbstractPath[] sorted = this.toArray(new AbstractPath[this.size()]);
        Arrays.sort(sorted, (Comparator<AbstractPath>) comparator);
        for(int j = 0; j < sorted.length; j++) {
            this.set(j, (E) sorted[j]);
        }
    }

    /**
     * Clear the list and all references.
     */
    @Override
    public void clear() {
        references.clear();
        super.clear();
    }
}