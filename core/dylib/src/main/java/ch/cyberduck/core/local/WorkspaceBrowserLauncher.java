package ch.cyberduck.core.local;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.binding.foundation.NSURL;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class WorkspaceBrowserLauncher implements BrowserLauncher {
    private static final Logger log = Logger.getLogger(WorkspaceBrowserLauncher.class);

    @Override
    public boolean open(final String url) {
        synchronized(NSWorkspace.class) {
            if(StringUtils.isNotBlank(url)) {
                if(NSWorkspace.sharedWorkspace().openURL(NSURL.URLWithString(url))) {
                    return true;
                }
                log.warn(String.format("Failure opening URL %s with browser", url));
            }
            return false;
        }
    }
}