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

/// <i>native declaration : :57</i>
public interface NSCoding extends NSObject {
    _Class CLASS = org.rococoa.Rococoa.createClass("NSCoding", _Class.class);

    public interface _Class extends org.rococoa.NSClass {
        NSCoding alloc();
    }

    /**
     * Original signature : <code>void encodeWithCoder(NSCoder*)</code><br>
     * <i>native declaration : :59</i>
     */
    void encodeWithCoder(NSCoder aCoder);

    /**
     * Original signature : <code>initWithCoder(NSCoder*)</code><br>
     * <i>native declaration : :60</i>
     */
    NSCoding initWithCoder(NSCoder aDecoder);
}
