package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.foundation.FoundationKitFunctions;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.FinderLocal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.cocoa.foundation.NSUInteger;

public class ApplicationSupportDirectoryFinder implements SupportDirectoryFinder {
    private static final Logger log = LogManager.getLogger(ApplicationSupportDirectoryFinder.class);

    private final Preferences preferences = PreferencesFactory.get();

    @Override
    public Local find() {
        final NSArray directories = FoundationKitFunctions.library.NSSearchPathForDirectoriesInDomains(
                FoundationKitFunctions.NSSearchPathDirectory.NSApplicationSupportDirectory,
                FoundationKitFunctions.NSSearchPathDomainMask.NSUserDomainMask,
                true);
        final String application = preferences.getProperty("application.name");
        if(directories.count().intValue() == 0) {
            log.error("Failed searching for application support directory");
            return new FinderLocal("~/Library/Application Support", application);
        }
        else {
            final String directory = directories.objectAtIndex(new NSUInteger(0)).toString();
            log.info("Found application support directory in {}", directory);
            final Local folder = new FinderLocal(directory, application);
            log.debug("Use folder {} for application support directory", folder);
            return folder;
        }
    }
}
