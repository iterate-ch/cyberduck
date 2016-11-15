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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A sortable list with a map to lookup values by key.
 */
public class AttributedList<E extends Referenceable> extends ArrayList<E> {
    private static final Logger log = Logger.getLogger(AttributedList.class);

    private static final long serialVersionUID = 8900332123622028341L;

    private static final AttributedList EMPTY = new AttributedList() {
        @Override
        public boolean add(final Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(final int index, final Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final int index, final Collection c) {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Metadata of file listing
     */
    private final AttributedListAttributes<E> attributes
            = new AttributedListAttributes<E>().withTimestamp(System.currentTimeMillis());

    /**
     * Initialize an attributed list with default attributes
     */
    public AttributedList() {
        //
    }

    /**
     * @param collection Default content
     */
    public AttributedList(final java.util.Collection<E> collection) {
        this.addAll(collection);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Referenceable> AttributedList<T> emptyList() {
        return (AttributedList<T>) EMPTY;
    }

    /**
     * Metadata of the list.
     *
     * @return File attributes
     */
    public AttributedListAttributes<E> attributes() {
        return attributes;
    }

    public E get(final E reference) {
        final int index = this.indexOf(reference);
        if(-1 == index) {
            return null;
        }
        return super.get(index);
    }

    /**
     * The CopyOnWriteArrayList iterator does not support remove but the sort implementation
     * makes use of it. Provide our own implementation here to circumvent.
     *
     * @param comparator The comparator to use
     * @see java.util.Collections#sort(java.util.List, java.util.Comparator)
     * @see java.util.concurrent.CopyOnWriteArrayList#iterator()
     */
    private void doSort(final Comparator<E> comparator) {
        if(null == comparator) {
            return;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Sort list %s with comparator %s", this, comparator));
        }
        Collections.sort(this, comparator);
    }

    /**
     * @param filter Filter
     * @return Unsorted filtered list
     */
    public AttributedList<E> filter(final Filter<E> filter) {
        return this.filter(null, filter);
    }

    public AttributedList<E> filter(final Comparator<E> comparator) {
        return this.filter(comparator, null);
    }

    /**
     * @param comparator The comparator to use
     * @param filter     Filter
     * @return Filtered list sorted with comparator
     */
    public AttributedList<E> filter(final Comparator<E> comparator, final Filter<E> filter) {
        boolean needsSorting = false;
        if(null != comparator) {
            needsSorting = !attributes.getComparator().equals(comparator);
        }
        boolean needsFiltering = false;
        if(null != filter) {
            needsFiltering = !attributes.getFilter().equals(filter);
        }
        if(needsSorting) {
            // Do not sort when the list has not been filtered yet
            if(!needsFiltering) {
                this.doSort(comparator);
            }
            // Saving last sorting comparator
            attributes.setComparator(comparator);
        }
        if(needsFiltering) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Filter list %s with filter %s", this, filter));
            }
            // Add previously hidden files to children
            final List<E> hidden = attributes.getHidden();
            if(!hidden.isEmpty()) {
                this.addAll(hidden);
                // Clear the previously set of hidden files
                hidden.clear();
            }
            for(Iterator<E> iter = this.iterator(); iter.hasNext(); ) {
                final E child = iter.next();
                if(!filter.accept(child)) {
                    // Child not accepted by filter; add to cached hidden files
                    attributes.addHidden(child);
                    // Remove hidden file from current file listing
                    iter.remove();
                }
            }
            // Saving last filter
            attributes.setFilter(filter);
            // Sort again because the list has changed
            this.doSort(comparator);
        }
        return this;
    }

    /**
     * Clear the list and all references.
     */
    @Override
    public void clear() {
        attributes.clear();
        super.clear();
    }
}