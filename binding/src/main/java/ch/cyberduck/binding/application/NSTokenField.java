package ch.cyberduck.binding.application;

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

import ch.cyberduck.binding.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSRect;

public abstract class NSTokenField extends NSTextField {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSTokenField", _Class.class);

    public static NSTokenField textfieldWithFrame(NSRect frameRect) {
        return CLASS.alloc().initWithFrame(frameRect);
    }

    public interface _Class extends ObjCClass {
        NSTokenField alloc();
    }

    @Override
    public abstract NSTokenField initWithFrame(NSRect frameRect);

    public abstract void setObjectValue(NSObject value);
}
