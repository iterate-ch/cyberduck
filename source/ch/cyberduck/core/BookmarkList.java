package ch.cyberduck.core;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Keeps track of user bookmarks
 *
 * @version $Id$
 * @see ch.cyberduck.core.Host
 */
public abstract class BookmarkList {
	private static BookmarkList instance;

    protected List data = new ArrayList();

    public BookmarkList() {
        this.load();
    }

	public static BookmarkList instance() {
        if (null == instance) {
            instance = new ch.cyberduck.ui.cocoa.CDBookmarkList();
        }
        return instance;
    }
	
    public abstract void save();

    public abstract void load();

    public abstract Host importBookmark(java.io.File file);

    public abstract void exportBookmark(Host bookmark, java.io.File file);

    // ----------------------------------------------------------
    //	Data Manipulation
    // ----------------------------------------------------------

    public void addItem(Host item) {
        this.data.add(item);
    }

    public void addItem(Host item, int row) {
		this.data.add(row, item);
    }

    public void removeItem(int index) {
        if (index < this.size()) {
			this.data.remove(index);
		}
    }

    public void removeItem(Host item) {
		this.removeItem(this.data.lastIndexOf(item));
    }

    public Host getItem(int row) {
		Host result = null;
        if (row < this.size()) {
			result = (Host) this.data.get(row);
		}
        return result;
    }

    public int indexOf(Object o) {
        return this.data.indexOf(o);
    }

    public Collection values() {
        return data;
    }

    public int size() {
        return this.data.size();
    }

    public void clear() {
        this.data.clear();
    }

    public Iterator iterator() {
        return data.iterator();
    }
}
