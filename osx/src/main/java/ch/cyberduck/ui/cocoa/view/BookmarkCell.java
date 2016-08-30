package ch.cyberduck.ui.cocoa.view;

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

import ch.cyberduck.binding.application.NSCell;

import org.rococoa.ObjCClass;

public abstract class BookmarkCell extends NSCell {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("CDBookmarkCell", _Class.class);

    /// <i>native declaration : :22</i>
    public static final int SMALL_BOOKMARK_SIZE = 16;
    /// <i>native declaration : :23</i>
    public static final int MEDIUM_BOOKMARK_SIZE = 32;
    /// <i>native declaration : :24</i>
    public static final int LARGE_BOOKMARK_SIZE = 64;

    public static BookmarkCell bookmarkCell() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        BookmarkCell alloc();
    }

    public abstract BookmarkCell init();
}