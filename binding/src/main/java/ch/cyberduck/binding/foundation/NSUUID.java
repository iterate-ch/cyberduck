package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.rococoa.ObjCClass;

/**
 * A universally unique value that can be used to identify types, interfaces, and other items.
 */
public abstract class NSUUID extends NSObject {
    private static final NSUUID._Class CLASS = org.rococoa.Rococoa.createClass("NSUUID", NSUUID._Class.class);

    public static NSUUID UUID() {
        return CLASS.UUID();
    }

    public static NSUUID UUID(final String string) {
        return CLASS.alloc().initWithUUIDString(string);
    }

    public interface _Class extends ObjCClass {
        NSUUID UUID();

        NSUUID alloc();
    }

    /**
     * @param string The source string containing the UUID. The standard format for UUIDs represented in ASCII is a string punctuated by hyphens, for example 68753A44-4D6F-1226-9C60-0050E4C00067.
     */
    public abstract NSUUID initWithUUIDString(String string);

    public abstract String UUIDString();
}
