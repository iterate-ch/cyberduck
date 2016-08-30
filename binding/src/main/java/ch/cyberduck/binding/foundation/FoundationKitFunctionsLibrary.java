package ch.cyberduck.binding.foundation;

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

import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.internal.RococoaTypeMapper;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;

public final class FoundationKitFunctionsLibrary {

    private FoundationKitFunctionsLibrary() {
        //
    }

    private static final FoundationKitFunctions instance = (FoundationKitFunctions) Native.loadLibrary(
            "Foundation", FoundationKitFunctions.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new RococoaTypeMapper()));

    public static NSRect NSUnionRect(NSRect aRect, NSRect bRect) {
        return instance.NSUnionRect(aRect, bRect);
    }

    /**
     * Original signature : <code>NSString* NSUserName()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:46</i>
     */
    public static String NSUserName() {
        return instance.NSUserName();
    }

    /**
     * Original signature : <code>public static NSString* NSFullUserName()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:47</i>
     */
    public static String NSFullUserName() {
        return instance.NSFullUserName();
    }

    /**
     * Original signature : <code>public static NSString* NSHomeDirectory()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:49</i>
     */
    public static String NSHomeDirectory() {
        return instance.NSHomeDirectory();
    }

    public static String NSHomeDirectoryForUser(String user) {
        return instance.NSHomeDirectoryForUser(user);
    }

    /**
     * Original signature : <code>public static NSString* NSTemporaryDirectory()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:52</i>
     */
    public static String NSTemporaryDirectory() {
        return instance.NSTemporaryDirectory();
    }

    public static NSArray NSSearchPathForDirectoriesInDomains(int directory, int domainMask, boolean expandTilde) {
        return instance.NSSearchPathForDirectoriesInDomains(directory, domainMask, expandTilde);
    }

    public static void NSLog(String format, String... args) {
        instance.NSLog(format, args);
    }

    public static CFStringRef CFStringCreateWithCharacters(final CFAllocatorRef allocator, final char[] chars, final CFIndex index) {
        return instance.CFStringCreateWithCharacters(allocator, chars, index);
    }
}
