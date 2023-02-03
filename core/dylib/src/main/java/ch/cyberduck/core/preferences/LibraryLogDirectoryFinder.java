package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.foundation.FoundationKitFunctions;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.local.FinderLocal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.cocoa.foundation.NSUInteger;

public class LibraryLogDirectoryFinder implements LogDirectoryFinder {
    private static final Logger log = LogManager.getLogger(LibraryLogDirectoryFinder.class);

    private final Preferences preferences = PreferencesFactory.get();

    @Override
    public Local find() {
        final NSArray directories = FoundationKitFunctions.library.NSSearchPathForDirectoriesInDomains(
            FoundationKitFunctions.NSSearchPathDirectory.NSLibraryDirectory,
            FoundationKitFunctions.NSSearchPathDomainMask.NSUserDomainMask,
            true);
        final String application = preferences.getProperty("application.name");
        if(directories.count().intValue() == 0) {
            log.error("Failed searching for library directory");
            return new FinderLocal("~/Library/Logs", application);
        }
        else {
            final String directory = directories.objectAtIndex(new NSUInteger(0)).toString();
            if(log.isInfoEnabled()) {
                log.info(String.format("Found library directory in %s", directory));
            }
            final Local folder = new FinderLocal(new FinderLocal(directory, "Logs"), application);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Use folder %s for log directory", folder));
            }
            return folder;
        }
    }
}
