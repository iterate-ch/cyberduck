package ch.cyberduck.core.preferences;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.internal.RococoaTypeMapper;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class ServiceManagementApplicationLoginRegistry implements ApplicationLoginRegistry {
    private static final Logger log = LogManager.getLogger(ServiceManagementApplicationLoginRegistry.class);

    private final LaunchServicesApplicationFinder finder
            = new LaunchServicesApplicationFinder();

    /**
     * This function works only with executables stored in the
     * <code>Contents/Library/LoginItems</code> directory of the bundle.
     */
    @Override
    public boolean register(final Application application) {
        final Local helper = new FinderLocal(new BundleApplicationResourcesFinder().find().getParent(),
                String.format("Library/LoginItems/%s.app", application.getName()));
        if(!finder.register(helper)) {
            log.warn(String.format("Failed to register %s (%s) with launch services", helper,
                    finder.getDescription(application.getIdentifier())));
        }
        if(!ServiceManagementFunctions.library.SMLoginItemSetEnabled(application.getIdentifier(), true)) {
            log.warn(String.format("Failed to register %s as login item", application));
            return false;
        }
        return true;
    }

    @Override
    public boolean unregister(final Application application) {
        if(!ServiceManagementFunctions.library.SMLoginItemSetEnabled(application.getIdentifier(), false)) {
            log.warn(String.format("Failed to remove %s as login item", application));
            return false;
        }
        return true;
    }

    public interface ServiceManagementFunctions extends Library {

        ServiceManagementFunctions library = Native.load(
            "ServiceManagement", ServiceManagementFunctions.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new RococoaTypeMapper()));

        /**
         * @param identifier The bundle identifier of the helper application bundle
         * @param enabled    The Boolean enabled state of the helper application. This value is effective only
         *                   for the currently logged in user. If true, the helper application will be started
         *                   immediately (and upon subsequent logins) and kept running. If false, the helper
         *                   application will no longer be kept running.
         * @return Returns true if the requested change has taken effect.
         */
        boolean SMLoginItemSetEnabled(String identifier, boolean enabled);
    }
}
