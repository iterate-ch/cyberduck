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

import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class Collection<E> extends ArrayList<E> implements CollectionListener<E> {
    private static final Logger log = LogManager.getLogger(Collection.class);

    private final ReentrantLock locked = new ReentrantLock();
    private final AtomicBoolean loaded = new AtomicBoolean();

    private final Set<CollectionListener<E>> listeners = new CopyOnWriteArraySet<>();

    public Collection() {
        super();
    }

    public Collection(java.util.Collection c) {
        super(c);
    }

    /**
     * Mark collection as loaded and notify listeners.
     */
    public void load() throws AccessDeniedException {
        this.collectionLoaded();
    }

    public void save() {
        //
    }

    public void addListener(CollectionListener<E> listener) {
        listeners.add(listener);
    }

    public void removeListener(CollectionListener<E> listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean addAll(java.util.Collection<? extends E> c) {
        final List<E> temporary = new ArrayList<>();
        for(E item : c) {
            if(temporary.contains(item)) {
                log.warn("Skip adding duplicate {}", item);
                continue;
            }
            temporary.add(item);
        }
        if(super.addAll(temporary)) {
            for(E item : temporary) {
                this.collectionItemAdded(item);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(final int index, final java.util.Collection<? extends E> c) {
        final List<E> temporary = new ArrayList<>();
        for(E item : c) {
            if(temporary.contains(item)) {
                log.warn("Skip adding duplicate {}", item);
                continue;
            }
            temporary.add(item);
        }
        if(super.addAll(index, temporary)) {
            for(E item : temporary) {
                this.collectionItemAdded(item);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean add(E item) {
        if(this.contains(item)) {
            log.warn("Skip adding duplicate {}", item);
            return false;
        }
        if(super.add(item)) {
            this.collectionItemAdded(item);
            return true;
        }
        return false;
    }

    @Override
    public void add(int row, E item) {
        if(this.contains(item)) {
            log.warn("Skip adding duplicate {}", item);
            return;
        }
        super.add(row, item);
        this.collectionItemAdded(item);
    }

    public void replace(int row, E item) {
        this.set(row, item);
        for(CollectionListener<E> listener : listeners) {
            listener.collectionItemChanged(item);
        }
    }

    @Override
    public void clear() {
        for(Iterator<E> iter = this.iterator(); iter.hasNext(); ) {
            iter.next();
            iter.remove();
        }
    }

    /**
     * @param row Index in collection
     * @return the element that was removed from the list.
     */
    @Override
    public E remove(int row) {
        E previous = super.remove(row);
        if(previous != null) {
            this.collectionItemRemoved(previous);
        }
        return previous;
    }

    @Override
    public boolean remove(Object item) {
        if(super.remove(item)) {
            this.collectionItemRemoved((E) item);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        if(super.removeAll(c)) {
            for(Object item : c) {
                this.collectionItemRemoved((E) item);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeIf(final Predicate<? super E> filter) {
        final Set<E> removed = new HashSet<>();
        final boolean r = super.removeIf(new Predicate<E>() {
            @Override
            public boolean test(final E e) {
                if(filter.test(e)) {
                    removed.add(e);
                    return true;
                }
                return false;
            }
        });
        for(E e : removed) {
            this.collectionItemRemoved(e);
        }
        return r;
    }

    @Override
    public void collectionLoaded() {
        loaded.set(true);
        if(this.isLocked()) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        for(CollectionListener<E> listener : listeners) {
            listener.collectionLoaded();
        }
    }

    @Override
    public void collectionItemAdded(final E item) {
        if(this.isLocked()) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        for(CollectionListener<E> listener : listeners) {
            listener.collectionItemAdded(item);
        }
    }

    @Override
    public void collectionItemRemoved(final E item) {
        if(this.isLocked()) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        for(CollectionListener<E> listener : listeners) {
            listener.collectionItemRemoved(item);
        }
    }

    @Override
    public void collectionItemChanged(final E item) {
        if(this.isLocked()) {
            log.debug("Do not notify changes of locked collection");
            return;
        }
        for(CollectionListener<E> listener : listeners) {
            listener.collectionItemChanged(item);
        }
    }

    public boolean isLoaded() {
        return loaded.get();
    }

    /**
     * @return True while loading and the collection is locked.
     */
    public boolean isLocked() {
        return locked.isLocked();
    }

    /**
     * Acquire lock to write to collection
     */
    protected void lock() {
        locked.lock();
    }

    /**
     * Release exclusive lock on collection
     */
    protected void unlock() {
        locked.unlock();
    }
}
