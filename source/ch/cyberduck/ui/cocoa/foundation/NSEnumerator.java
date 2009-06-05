package ch.cyberduck.ui.cocoa.foundation;

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

/// <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSEnumerator.h:33</i>
public abstract class NSEnumerator extends NSObject {

    /**
     * Original signature : <code>id nextObject()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSEnumerator.h:35</i>
     */
    public abstract NSObject nextObject();

    /**
     * Original signature : <code>NSArray* allObjects()</code><br>
     * <i>from NSExtendedEnumerator native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSEnumerator.h:41</i>
     */
    public abstract NSArray allObjects();
}
