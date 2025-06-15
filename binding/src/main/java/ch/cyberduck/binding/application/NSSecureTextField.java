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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSRect;

public abstract class NSSecureTextField extends NSTextField {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSSecureTextField", _Class.class);

    public static NSSecureTextField textfieldWithFrame(NSRect frameRect) {
        return CLASS.alloc().initWithFrame(frameRect);
    }

    public static NSSecureTextField textFieldWithString(final String stringValue) {
        return CLASS.textFieldWithString(stringValue);
    }

    public interface _Class extends ObjCClass {
        NSSecureTextField alloc();

        /**
         * Initializes a single-line editable text field for user input using the system default font and standard visual appearance.
         *
         * @param stringValue A string to use as the initial content of the editable text field.
         * @return A single-line editable text field that displays the specified string.
         */
        NSSecureTextField textFieldWithString(String stringValue);
    }

    @Override
    public abstract NSSecureTextField initWithFrame(NSRect frameRect);
}
