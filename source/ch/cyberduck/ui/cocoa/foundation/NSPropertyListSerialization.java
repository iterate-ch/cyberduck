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

import org.rococoa.NSObjectByReference;

import java.io.IOException;

/// <i>native declaration : :27</i>
public abstract class NSPropertyListSerialization implements NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSPropertyListSerialization", _Class.class);

    public static NSObject propertyListFromData(NSData data) throws IOException {
        NSObjectByReference errorReference = new NSObjectByReference();
        final NSObject result = CLASS.propertyListFromData_mutabilityOption_format_errorDescription(
                data,
                NSPropertyListSerialization.PropertyListImmutable,
                NSPropertyListSerialization.PropertyListXMLFormat, errorReference);
        if(!errorReference.getValueAs(NSString.class).id().isNull()) {
            throw new IOException(errorReference.getValueAs(NSString.class).toString());
        }
        return result;
    }

    public static NSData dataFromPropertyList(NSObject plist) throws IOException {
        NSObjectByReference errorReference = new NSObjectByReference();
        final NSData result = CLASS.dataFromPropertyList_format_errorDescription(
                plist,
                NSPropertyListSerialization.PropertyListXMLFormat, errorReference);
        if(!errorReference.getValueAs(NSString.class).id().isNull()) {
            throw new IOException(errorReference.getValueAs(NSString.class).toString());
        }
        return result;
    }

    public interface _Class extends org.rococoa.NSClass {
        /**
         * <i>native declaration : :31</i><br>
         * Conversion Error : /// Original signature : <code>BOOL propertyList(null, NSPropertyListFormat)</code><br>
         * + (BOOL)propertyList:(null)plist isValidForFormat:(NSPropertyListFormat)format; (Argument plist cannot be converted)
         */
        /**
         * <i>native declaration : :32</i><br>
         * Conversion Error : /// Original signature : <code>NSData* dataFromPropertyList(null, NSPropertyListFormat, NSString**)</code><br>
         * + (NSData*)dataFromPropertyList:(null)plist format:(NSPropertyListFormat)format errorDescription:(NSString**)errorString; (Argument plist cannot be converted)
         */
        NSData dataFromPropertyList_format_errorDescription(NSObject plist, int format, com.sun.jna.ptr.ByReference errorString);

        /**
         * Original signature : <code>propertyListFromData(NSData*, NSPropertyListMutabilityOptions, NSPropertyListFormat*, NSString**)</code><br>
         * <i>native declaration : :33</i>
         */
        NSObject propertyListFromData_mutabilityOption_format_errorDescription(NSData data, int opt, int format, com.sun.jna.ptr.ByReference errorString);
    }

    public static final int PropertyListImmutable = 0;
    public static final int PropertyListMutableContainers = 1;
    public static final int PropertyListMutableContainersAndLeaves = 2;
    public static final int PropertyListOpenStepFormat = 1;
    public static final int PropertyListXMLFormat = 100;
    public static final int PropertyListBinaryFormat = 200;

}

