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
import java.util.concurrent.locks.ReentrantLock;

/**
 * @version $Id$
 */
public class Collection<E> extends ArrayList<E> implements CollectionListener<E> {
    private static Logger log = Logger.getLogger(Collection.class);

    /**
     *
     */
    private ReentrantLock locked = new ReentrantLock();

    /**
     *
     */
    private boolean loaded;

    public Collection() {
        super();
    }

    public Collection(java.util.Collection<E> c) {
        super(c);
    }

    /**
     * Mark collection as loaded and notify listeners.
     */
    public void load() {
        this.loaded = true;
        this.collectionLoaded();
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
        for(E item : es) {
            this.collectionItemAdded(item);
        }
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
    public void clear() {
        for(Iterator<E> iter = this.iterator(); iter.hasNext();) {
            E next = iter.next();
            iter.remove();
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

    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        boolean modified = false;
        Iterator<?> e = iterator();
        while(e.hasNext()) {
            Object item = e.next();
            if(c.contains(item)) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    public void collectionLoaded() {
        if(this.isLocked()) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        for(CollectionListener<E> listener : listeners.toArray(new CollectionListener[listeners.size()])) {
            listener.collectionLoaded();
        }
    }

    public void collectionItemAdded(E item) {
        if(this.isLocked()) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        for(CollectionListener<E> listener : listeners.toArray(new CollectionListener[listeners.size()])) {
            listener.collectionItemAdded(item);
        }
    }

    public void collectionItemRemoved(E item) {
        if(this.isLocked()) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        for(CollectionListener<E> listener : listeners.toArray(new CollectionListener[listeners.size()])) {
            listener.collectionItemRemoved(item);
        }
    }

    public void collectionItemChanged(E item) {
        if(this.isLocked()) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        for(CollectionListener<E> listener : listeners.toArray(new CollectionListener[listeners.size()])) {
            listener.collectionItemChanged(item);
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * @return True while loading
     */
    public boolean isLocked() {
        return locked.isLocked();
    }

    /**
     *
     */
    protected void lock() {
        locked.lock();
    }

    /**
     *
     */
    protected void unlock() {
        locked.unlock();
    }
}