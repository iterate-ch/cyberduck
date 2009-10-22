package ch.cyberduck.ui.cocoa.application;

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

import org.rococoa.ObjCClass;

/// <i>native declaration : :10</i>
public abstract class NSActionCell extends NSCell {

    public interface _Class extends ObjCClass {
        NSActionCell alloc();
    }

    /**
     * Original signature : <code>id target()</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract org.rococoa.ID target();

    /**
     * Original signature : <code>void setTarget(id)</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract void setTarget(org.rococoa.ID anObject);
}
