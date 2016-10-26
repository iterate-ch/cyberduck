package ch.cyberduck.ui.cocoa.delegate;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.CollectionListener;

import org.rococoa.cocoa.foundation.NSInteger;

public abstract class CollectionMenuDelegate<T> extends AbstractMenuDelegate implements CollectionListener<T> {

    private final Collection<T> collection;

    public CollectionMenuDelegate(Collection<T> c) {
        this.collection = c;
        this.collection.addListener(this);
    }

    @Override
    public NSInteger numberOfItemsInMenu(final NSMenu menu) {
        if(this.isPopulated()) {
            // If you return a negative value, the number of items is left unchanged
            // and menu:updateItem:atIndex:shouldCancel: is not called.
            return new NSInteger(-1);
        }
        if(collection.size() > 0) {
            // The number of history plus a delimiter and the 'Clear' menu
            return new NSInteger(collection.size());
        }
        return new NSInteger(1);
    }

    public T itemForIndex(final NSInteger index) {
        return collection.get(index.intValue());
    }

    @Override
    public void collectionItemAdded(T item) {
        this.setNeedsUpdate(true);
    }

    @Override
    public void collectionItemRemoved(T item) {
        this.setNeedsUpdate(true);
    }

    @Override
    public void collectionItemChanged(T item) {
        this.setNeedsUpdate(true);
    }

    @Override
    public void collectionLoaded() {
        this.setNeedsUpdate(true);
    }

    @Override
    public void invalidate() {
        this.collection.removeListener(this);
        super.invalidate();
    }
}
