package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import org.rococoa.ObjCClass;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.cocoa.foundation.NSUInteger;

import com.sun.jna.Pointer;

public abstract class NSPropertyListSerialization extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSPropertyListSerialization", _Class.class);

    /**
     * NSPropertyListFormat
     */
    public static final int NSPropertyListOpenStepFormat = 1;
    public static final int NSPropertyListXMLFormat_v1_0 = 100;
    public static final int NSPropertyListBinaryFormat_v1_0 = 200;

    /**
     * NSPropertyListMutabilityOptions
     */
    public static final int NSPropertyListImmutable = 0;
    public static final int NSPropertyListMutableContainers = 1;
    public static final int NSPropertyListMutableContainersAndLeaves = 2;

    /**
     * Original signature : <code>+(NSData*)dataWithPropertyList:(id) format:(NSPropertyListFormat) options:(NSPropertyListWriteOptions) error:(NSError**)</code>
     *
     * @param plist   Property list object
     * @param format  Property list format
     * @param options Currently unused and must be 0
     * @param error   Error on failure
     * @return Serialized property list or null on failure
     */
    public static NSData dataWithPropertyList(final NSObject plist, final int format, final int options, final ObjCObjectByReference error) {
        return CLASS.dataWithPropertyList_format_options_error(plist, new NSUInteger(format), new NSUInteger(options), error);
    }

    /**
     * Original signature : <code>+(id)propertyListWithData:(NSData*) options:(NSPropertyListReadOptions) format:(NSPropertyListFormat*) error:(NSError**)</code>
     *
     * @param data    Serialized property list
     * @param options Mutability options
     * @param format  Format of the property list read or null
     * @param error   Error on failure
     * @return Property list object or null on failure
     */
    public static NSObject propertyListWithData(final NSData data, final int options, final Pointer format, final ObjCObjectByReference error) {
        return CLASS.propertyListWithData_options_format_error(data, new NSUInteger(options), format, error);
    }

    public interface _Class extends ObjCClass {
        NSData dataWithPropertyList_format_options_error(NSObject plist, NSUInteger format, NSUInteger options, ObjCObjectByReference error);

        NSObject propertyListWithData_options_format_error(NSData data, NSUInteger options, Pointer format, ObjCObjectByReference error);
    }
}
