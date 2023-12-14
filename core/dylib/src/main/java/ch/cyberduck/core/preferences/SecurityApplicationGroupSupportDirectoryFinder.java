package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.binding.foundation.NSFileManager;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.FinderLocal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SecurityApplicationGroupSupportDirectoryFinder implements SupportDirectoryFinder {
    private static final Logger log = LogManager.getLogger(SecurityApplicationGroupSupportDirectoryFinder.class);

    private final String identifier;

    public SecurityApplicationGroupSupportDirectoryFinder() {
        this(String.format("%s.%s",
                PreferencesFactory.get().getProperty("application.container.teamidentifier"),
                PreferencesFactory.get().getProperty("application.container.name")));
    }

    public SecurityApplicationGroupSupportDirectoryFinder(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Local find() {
        final NSFileManager manager = NSFileManager.defaultManager();
        final NSURL group = manager
                .containerURLForSecurityApplicationGroupIdentifier(identifier);
        if(null == group) {
            log.warn("Missing com.apple.security.application-groups in sandbox entitlements");
        }
        else {
            // You should organize the contents of this directory in the same way that any other Library folder is organized
            final String application = PreferencesFactory.get().getProperty("application.datafolder.name");
            return new FinderLocal(String.format("%s/Library/Application Support", group.path()), application);
        }
        log.warn("Missing support for security application groups. Default to application support directory");
        // Fallback for 10.7 and earlier
        return new ApplicationSupportDirectoryFinder().find();
    }
}
