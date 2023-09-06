package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.Rococoa;

/**
 * macOS 13.0+
 */
public abstract class SMAppService extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("SMAppService", _Class.class);

    public interface _Class extends ObjCClass {
        SMAppService loginItemServiceWithIdentifier(String identifier);

        SMAppService mainAppService();

        void openSystemSettingsLoginItems();
    }

    /**
     * Initializes an app service object for a login item corresponding to the bundle with the identifier you provide.
     *
     * @param identifier The bundle identifier of the helper application.
     * @return The property list name must correspond to a property list in the calling app’s Contents/Library/LoginItems directory
     */
    public static SMAppService loginItemServiceWithIdentifier(final String identifier) {
        return CLASS.loginItemServiceWithIdentifier(identifier);
    }

    /**
     * Opens System Settings to the Login Items control panel.
     */
    public static void openSystemSettingsLoginItems() {
        CLASS.openSystemSettingsLoginItems();
    }

    /**
     * An app service object that corresponds to the main application as a login item. Use this SMAppService to configure the main app to launch at login.
     */
    public static SMAppService mainAppService() {
        return CLASS.mainAppService();
    }

    /**
     * Registers the service so it can begin launching subject to user approval.
     * <p>
     * If the service corresponds to a LoginItem bundle, the helper starts immediately and on subsequent logins.
     * If the helper crashes or exits with a non-zero status, the system relaunches it.
     *
     * @return Returns YES if the service was successfully registered; otherwise, NO.
     */
    public abstract boolean registerAndReturnError(ObjCObjectByReference error);

    /**
     * Unregisters the service so the system no longer launches it.
     *
     * @param error Upon an unsuccessful return, a new NSError object describing the error. Upon successful return, this argument is NULL. This argument may be NULL.
     * @return Returns YES if the service was successfully unregistered; otherwise, NO.
     */
    public abstract boolean unregisterAndReturnError(ObjCObjectByReference error);
}
