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
public class Collection<E> extends ArrayList<E> implements CollectionListener<E> {

    public Collection() {
        super();
    }

    public Collection(java.util.Collection<E> c) {
        super(c);
    }

    @Override
    public int indexOf(Object elem) {
        for(int i = 0; i < this.size(); i++) {
            if(this.get(i).equals(elem)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object elem) {
        for(int i = this.size() - 1; i >= 0; i--) {
            if(this.get(i).equals(elem)) {
                return i;
            }
        }
        return -1;
    }

    private Set<CollectionListener<E>> listeners
            = Collections.synchronizedSet(new HashSet<CollectionListener<E>>());

    public void addListener(CollectionListener<E> listener) {
        listeners.add(listener);
    }

    public void removeListener(CollectionListener<E> listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean addAll(java.util.Collection<? extends E> es) {
        super.addAll(es);
        this.collectionItemAdded(null);
        return true;
    }

    @Override
    public boolean add(E object) {
        super.add(object);
        this.collectionItemAdded(object);
        return true;
    }

    @Override
    public void add(int row, E object) {
        super.add(row, object);
        this.collectionItemAdded(object);
    }

    @Override
    public E get(int row) {
        return super.get(row);
    }

    @Override
    public Iterator<E> iterator() {
        return super.iterator();
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public void clear() {
        for(Iterator<E> iter = this.iterator(); iter.hasNext();) {
            E next = iter.next();
            iter.remove();
            this.collectionItemRemoved(next);
        }
    }

    /**
     * @param row
     * @return the element that was removed from the list.
     */
    @Override
    public E remove(int row) {
        E previous = super.remove(row);
        this.collectionItemRemoved(previous);
        return previous;
    }

    @Override
    public boolean remove(Object item) {
        boolean previous = super.remove(item);
        this.collectionItemRemoved((E) item);
        return previous;
    }

    public void collectionItemAdded(E item) {
        for(CollectionListener<E> listener : listeners.toArray(new CollectionListener[listeners.size()])) {
            listener.collectionItemAdded(item);
        }
    }

    public void collectionItemRemoved(E item) {
        for(CollectionListener<E> listener : listeners.toArray(new CollectionListener[listeners.size()])) {
            listener.collectionItemRemoved(item);
        }
    }

    public void collectionItemChanged(E item) {
        for(CollectionListener<E> listener : listeners.toArray(new CollectionListener[listeners.size()])) {
            listener.collectionItemChanged(item);
        }
    }
}