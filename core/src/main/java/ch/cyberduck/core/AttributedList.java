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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A sortable list with a map to lookup values by key.
 */
public class AttributedList<E extends Referenceable> implements Iterable<E> {
    private static final Logger log = LogManager.getLogger(AttributedList.class);

    public static final AttributedList EMPTY = new AttributedList() {
        @Override
        public boolean add(final Referenceable o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(final int index, final Referenceable element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Iterable c) {
            throw new UnsupportedOperationException();
        }
    };

    private final List<E> impl
            = new CopyOnWriteArrayList<>();

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
    public AttributedList(final Iterable<E> collection) {
        for(E e : collection) {
            this.add(e);
        }
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

    public boolean add(final E e) {
        return impl.add(e);
    }

    public void add(final int index, final E e) {
        impl.add(index, e);
    }

    public boolean addAll(final Iterable<? extends E> c) {
        for(E file : c) {
            this.add(file);
        }
        return true;
    }

    public E get(final int index) {
        return impl.get(index);
    }

    public E get(final E reference) {
        final int index = impl.indexOf(reference);
        if(-1 == index) {
            return null;
        }
        return impl.get(index);
    }

    public void set(final int i, final E e) {
        impl.set(i, e);
    }

    @Override
    public Iterator<E> iterator() {
        return impl.iterator();
    }

    /**
     * Return filtered list
     *
     * @param filter Filter
     * @return Unsorted filtered list. Does not modify this list but returns a copy instead.
     */
    public AttributedList<E> filter(final Filter<E> filter) {
        return this.filter(null, filter);
    }

    /**
     * Return sorted list
     *
     * @param comparator Sorting comparator
     * @return Does not modify this list but returns a copy instead.
     */
    public AttributedList<E> filter(final Comparator<E> comparator) {
        return this.filter(comparator, null);
    }

    /**
     * @param comparator The comparator to use
     * @param filter     Filter
     * @return Filtered list sorted with comparator. Does not modify this list but returns a copy instead.
     */
    public AttributedList<E> filter(final Comparator<E> comparator, final Filter<E> filter) {
        return this.filter(new AttributedList<>(impl), comparator, filter);
    }

    /**
     * @param filtered   Result set
     * @param comparator The comparator to use
     * @param filter     Filter
     * @return Filtered list
     */
    public AttributedList<E> filter(final AttributedList<E> filtered, final Comparator<E> comparator, final Filter<E> filter) {
        if(null != comparator) {
            log.debug("Sort list {} with comparator {}", this, comparator);
            filtered.impl.sort(comparator);
        }
        if(null != filter) {
            log.debug("Filter list {} with filter {}", this, filter);
            filtered.impl.removeIf(e -> !filter.accept(e));
        }
        return filtered;
    }

    /**
     * Clear the list and all references.
     */
    public void clear() {
        impl.clear();
    }

    public boolean isEmpty() {
        return impl.isEmpty();
    }

    public int size() {
        return impl.size();
    }

    public boolean contains(final E e) {
        return impl.contains(e);
    }

    public E find(final Predicate<E> predicate) {
        final Optional<E> optional = impl.stream().filter(predicate).findFirst();
        return optional.orElse(null);
    }

    public Set<E> findAll(final Predicate<E> predicate) {
        return impl.stream().filter(predicate).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public E[] toArray() {
        return (E[]) impl.toArray(new Referenceable[impl.size()]);
    }

    public List<E> toList() {
        return impl;
    }

    public Stream<E> toStream() {
        return impl.parallelStream();
    }

    public int indexOf(final E e) {
        return impl.indexOf(e);
    }

    public void remove(final int index) {
        impl.remove(index);
    }

    public boolean remove(final E e) {
        return impl.remove(e);
    }

    public boolean removeAll(final java.util.Collection<E> e) {
        return impl.removeAll(e);
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof AttributedList)) {
            return false;
        }
        final AttributedList<?> that = (AttributedList<?>) o;
        return Objects.equals(impl, that.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }
}
