package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import org.rococoa.internal.RococoaTypeMapper;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;

public final class ServiceManagementLibrary {

    private ServiceManagementLibrary() {
        //
    }

    private static final ServiceManagementFunctions instance = (ServiceManagementFunctions) Native.loadLibrary(
            "ServiceManagement", ServiceManagementFunctions.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new RococoaTypeMapper()));

    public static boolean SMLoginItemSetEnabled(final String bundleIdentifier, boolean enabled) {
        return instance.SMLoginItemSetEnabled(CFStringRef.toCFString(bundleIdentifier), enabled);
    }
}
