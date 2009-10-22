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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSSize;

/// <i>native declaration : /Users/dkocher/null:10</i>
public abstract class NSValue extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSValue", _Class.class);

    public static NSValue valueWithSize(NSSize size) {
        return CLASS.valueWithSize(size);
    }

    public interface _Class extends ObjCClass {
        NSValue valueWithSize(NSSize size);
    }

    /**
     * Original signature : <code>void getValue(void*)</code><br>
     * <i>native declaration : /Users/dkocher/null:12</i>
     */
    public abstract void getValue(com.sun.jna.Pointer value);

    /**
     * Original signature : <code>const char* objCType()</code><br>
     * <i>native declaration : /Users/dkocher/null:13</i>
     */
    public abstract com.sun.jna.ptr.ByteByReference objCType();

    /**
     * Original signature : <code>initWithBytes(const void*, const char*)</code><br>
     * <i>from NSValueCreation native declaration : /Users/dkocher/null:19</i>
     */
    public abstract NSValue initWithBytes_objCType(com.sun.jna.Pointer value, java.lang.String type);

    /**
     * Original signature : <code>nonretainedObjectValue()</code><br>
     * <i>from NSValueExtensionMethods native declaration : /Users/dkocher/null:28</i>
     */
    public abstract NSObject nonretainedObjectValue();

    /**
     * Original signature : <code>void* pointerValue()</code><br>
     * <i>from NSValueExtensionMethods native declaration : /Users/dkocher/null:31</i>
     */
    public abstract com.sun.jna.Pointer pointerValue();

    /**
     * Original signature : <code>BOOL isEqualToValue(NSValue*)</code><br>
     * <i>from NSValueExtensionMethods native declaration : /Users/dkocher/null:33</i>
     */
    public abstract byte isEqualToValue(NSValue value);
}
