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
import ch.cyberduck.core.LocalFactory;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;

/**
 * @version $Id$
 */
public class SecurityApplicationGroupSupportDirectoryFinder implements SupportDirectoryFinder {
    private static final Logger log = Logger.getLogger(SecurityApplicationGroupSupportDirectoryFinder.class);

    @Override
    public Local find() {
        final NSFileManager manager = NSFileManager.defaultManager();
        if(manager.respondsToSelector(Foundation.selector("containerURLForSecurityApplicationGroupIdentifier:"))) {
            final NSURL group = manager
                    .containerURLForSecurityApplicationGroupIdentifier("G69SCX94XU.duck");
            if(null == group) {
                log.warn("Missing com.apple.security.application-groups in sandbox entitlements");
            }
            else {
                return LocalFactory.get(group.path());
            }
        }
        log.warn("Missing support for security application groups. Default to application support directory");
        // Fallback for 10.7 and earlier
        return new ApplicationSupportDirectoryFinder().find();
    }
}
