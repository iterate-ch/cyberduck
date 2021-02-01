package ch.cyberduck.core.urlhandler;/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSURL;

import org.rococoa.ObjCObjectByReference;
import org.rococoa.internal.RococoaTypeMapper;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface LaunchServicesLibrary extends Library {
    LaunchServicesLibrary library = Native.load(
        "CoreServices", LaunchServicesLibrary.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new RococoaTypeMapper()));


    /**
     * Returns the app that opens an item.
     * <p>
     * Consults the binding tables to return the application that would be used to open inURL if it were double-clicked
     * in the Finder. This application will be the user-specified override if appropriate or the default otherwise.
     *
     * @param inURL      The URL of the item for which the app is requested.
     * @param inRoleMask Whether to return the editor or viewer for inURL. If you don't care which, use kLSRolesAll.
     * @param outError   On failure, set to a CFErrorRef describing the problem. If you are not interested in this
     *                   information, pass NULL. The caller is responsible for releasing this object.
     * @return If an acceptable app is found, its URL is returned. If the URL is a file:// URL, the application bound to
     * the specified file or directory's type is returned. If the URL's scheme is something else, its default scheme
     * handler is returned. If no app could be found, NULL is returned and outError (if not NULL) is populated with
     * kLSApplicationNotFoundErr. The caller is responsible for releasing this URL.
     */
    NSURL LSCopyDefaultApplicationURLForURL(NSURL inURL, int inRoleMask, ObjCObjectByReference outError);

    /**
     * Locates all known apps suitable for opening an item for the specified URL.
     * <p>
     * If the item URL’s scheme is file (designating either a file or a directory), the selection of suitable
     * applications is based on the designated item’s filename extension, file type, and creator signature, along with
     * the role specified by the inRolesMask parameter. Otherwise, the selection is based on the URL scheme (such as
     * http, ftp, or mailto).
     *
     * @param inURL      A Core Foundation URL reference designating the item for which all suitable apps are requested.
     *                   See CFURL for a description of the CFURLRef data type.
     * @param inRoleMask A bit mask specifying the apps’ role or roles with respect to the designated item. See
     *                   LSRolesMask for a description of this mask. This parameter applies only to URLs with a scheme
     *                   component of file, and is ignored for all other schemes. If the role is unimportant, pass
     *                   kLSRolesAll.
     * @return An array of Core Foundation URL references, one for each app that can open the designated item with at
     * least one of the specified roles. You are responsible for releasing the array object. If no suitable apps are
     * found in the Launch Services database, the function will return NULL.
     * <p>
     * In macOS 10.15 and later, the returned array is sorted with the first element containing the best available apps
     * for opening the specified URL. Prior to macOS 10.15, the order of elements in the array was undefined.
     */
    NSArray LSCopyApplicationURLsForURL(NSURL inURL, int inRoleMask);

    /**
     * Sets the user’s preferred default handler for the specified URL scheme.
     * <p>
     * Call LSCopyDefaultHandlerForURLScheme to get the current setting of the user’s preferred default handler for a
     * specified content type.
     * <p>
     * URL handling capability is determined according to the value of the CFBundleURLTypes key in an app’s Info.plist.
     * For information on the CFBundleURLTypes key, see the section “CFBundleURLTypes” in macOS Runtime Configuration
     * Guidelines.
     *
     * @param inURLScheme       The URL scheme for which the handler is to be set.
     * @param inHandlerBundleID The bundle identifier that is to be set as the handler for the URL scheme specified by
     *                          inURLScheme.
     * @return A result code; see Result Codes.
     */
    int LSSetDefaultHandlerForURLScheme(String inURLScheme, String inHandlerBundleID);

    int kLSRolesNone = 0x00000001;
    int kLSRolesViewer = 0x00000002;
    int kLSRolesEditor = 0x00000004;
    int kLSRolesShell = 0x00000008;
    int kLSRolesAll = 0xFFFFFFFF;
}
