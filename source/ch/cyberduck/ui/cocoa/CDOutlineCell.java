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

import org.rococoa.ObjCClass;

/**
 * @version $Id$
 */
public abstract class CDOutlineCell extends NSTextFieldCell {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("CDOutlineCell", _Class.class);

    public static CDOutlineCell outlineCell() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        CDOutlineCell alloc();
    }

    @Override
    public abstract CDOutlineCell init();

    public abstract void setIcon(NSImage aImage);

}