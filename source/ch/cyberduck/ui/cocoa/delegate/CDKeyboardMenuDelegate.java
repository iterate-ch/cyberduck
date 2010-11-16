package ch.cyberduck.ui.cocoa.delegate;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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

import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ObjCClass;

/**
 * @version $Id$
 */
public abstract class CDKeyboardMenuDelegate extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("CDKeyboardMenuDelegate", _Class.class);

    public static CDKeyboardMenuDelegate create() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        CDKeyboardMenuDelegate alloc();
    }

    public abstract CDKeyboardMenuDelegate init();
}
