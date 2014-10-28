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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A sortable list with a map to lookup values by key.
 *
 * @version $Id$
 */
public class AttributedList<E extends Referenceable> extends ArrayList<E> {
    private static final Logger log = Logger.getLogger(AttributedList.class);

    private static final long serialVersionUID = 8900332123622028341L;

    /**
     * Metadata of file listing
     */
    private AttributedListAttributes<E> attributes
            = new AttributedListAttributes<E>();

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

    public static <T extends Referenceable> AttributedList<T> emptyList() {
        return new AttributedList<T>();
    }

    /**
     * Metadata of the list.
     *
     * @return File attributes
     */
    public AttributedListAttributes<E> attributes() {
        return attributes;
    }

    /**
     * Additional key,value table to lookup paths by reference
     */
    private Map<PathReference, E> references
            = new ConcurrentHashMap<PathReference, E>();

    @Override
    public boolean add(E path) {
        final E previous = references.put(path.getReference(), path);
        if(null != previous) {
            log.warn(String.format("Replacing %s with %s in file listing.", previous, path));
        }
        return super.add(path);
    }

    @Override
    public boolean addAll(java.util.Collection<? extends E> c) {
        for(E path : c) {
            final E previous = references.put(path.getReference(), path);
            if(null != previous) {
                log.warn(String.format("Replacing %s with %s in file listing.", previous, path));
            }
        }
        return super.addAll(c);
    }

    public E get(final PathReference reference) {
        return references.get(reference);
    }

    public boolean contains(final PathReference reference) {
        return references.containsKey(reference);
    }

    public int indexOf(final PathReference reference) {
        return super.indexOf(references.get(reference));
    }

    @Override
    public E remove(final int index) {
        final E removed = super.remove(index);
        references.remove(removed.getReference());
        return removed;
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
        Collections.sort(this, comparator);
    }

    /**
     * @param filter Filter
     * @return Unsorted filtered list
     */
    public AttributedList<E> filter(final Filter filter) {
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
            // Add previously hidden files to children
            final List<E> hidden = attributes.getHidden();
            this.addAll(hidden);
            // Clear the previously set of hidden files
            hidden.clear();
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
        references.clear();
        attributes.clear();
        super.clear();
    }
}