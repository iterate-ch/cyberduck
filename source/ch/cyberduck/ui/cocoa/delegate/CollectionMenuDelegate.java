package ch.cyberduck.ui.cocoa.delegate;

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.CollectionListener;

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

/**
 * @version $Id$
 */
public abstract class CollectionMenuDelegate<T> extends AbstractMenuDelegate implements CollectionListener<T> {

    private Collection<T> collection;

    public CollectionMenuDelegate(Collection<T> c) {
        this.collection = c;
        this.collection.addListener(this);
    }

    public void collectionItemAdded(T item) {
        this.setNeedsUpdate(true);
    }

    public void collectionItemRemoved(T item) {
        this.setNeedsUpdate(true);
    }

    public void collectionItemChanged(T item) {
        this.setNeedsUpdate(true);
    }

    @Override
    protected void invalidate() {
        this.collection.removeListener(this);
        super.invalidate();
    }
}
