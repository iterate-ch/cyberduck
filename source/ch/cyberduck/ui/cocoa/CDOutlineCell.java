package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.application.NSTextFieldCell;

import org.rococoa.Rococoa;

/**
 * @version $Id$
 */
public interface CDOutlineCell extends NSTextFieldCell {
    static final _Class CLASS = org.rococoa.Rococoa.createClass("CDOutlineCell", _Class.class);

    public static class Factory {
        public static CDOutlineCell create() {
            return Rococoa.cast(CLASS.alloc().init().autorelease(), CDOutlineCell.class);
        }
    }

    public interface _Class extends org.rococoa.NSClass {
        CDOutlineCell alloc();
    }

    public abstract CDOutlineCell init();

    public abstract void setIcon(NSImage aImage);

}