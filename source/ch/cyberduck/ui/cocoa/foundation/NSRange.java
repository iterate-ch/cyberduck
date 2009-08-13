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

import org.rococoa.cocoa.CFIndex;
import org.rococoa.cocoa.CFRange;
import org.rococoa.cocoa.foundation.NSUInteger;

public class NSRange extends CFRange {

    public static NSRange NSMakeRange(NSUInteger loc, NSUInteger len) {
        CFIndex cfLocation = new CFIndex();
        cfLocation.setValue(loc.longValue());
        CFIndex cfLength = new CFIndex();
        cfLength.setValue(len.longValue());
        return new NSRange(cfLocation, cfLength);
    }

    public NSRange(final CFIndex location, final CFIndex length) {
        super(location, length);
    }
}
