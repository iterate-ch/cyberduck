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

import ch.cyberduck.ui.cocoa.application.NSCell;
import ch.cyberduck.ui.cocoa.application.NSView;

import org.rococoa.ObjCClass;

/**
 * @version $Id$
 */
public abstract class CDControllerCell extends NSCell {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("CDControllerCell", _Class.class);

    public static CDControllerCell controllerCell() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        CDControllerCell alloc();
    }

    public abstract CDControllerCell init();

    /**
     *
     * @param aImage
     */
    public abstract void setView(NSView aImage);
}