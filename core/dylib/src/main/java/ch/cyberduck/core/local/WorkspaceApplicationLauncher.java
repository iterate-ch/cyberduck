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
import ch.cyberduck.binding.foundation.NSDistributedNotificationCenter;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.core.Local;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WorkspaceApplicationLauncher implements ApplicationLauncher {
    private static final Logger log = LogManager.getLogger(WorkspaceApplicationLauncher.class);

    private final NSWorkspace workspace = NSWorkspace.sharedWorkspace();

    @Override
    public boolean open(final Local file) {
        synchronized(NSWorkspace.class) {
            if(!workspace.openFile(file.getAbsolute())) {
                log.warn("Error opening file {}", file);
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean open(final Local file, final Application application) {
        synchronized(NSWorkspace.class) {
            final String path = workspace.absolutePathForAppBundleWithIdentifier(application.getIdentifier());
            if(StringUtils.isNotBlank(path)) {
                if(workspace.openFile(file.getAbsolute(), path)) {
                    return true;
                }
            }
            log.warn("Error opening file {} with application {}", file, application);
            return false;
        }
    }

    @Override
    public boolean open(final Application application, final String args) {
        synchronized(NSWorkspace.class) {
            // Open application by name should work without any special entitlements when sandboxed
            return workspace.launchApplication(application.getName());
        }
    }

    /**
     * Post a download finished notification to the distributed notification center. Will cause the download folder to
     * bounce just once.
     */
    @Override
    public void bounce(final Local file) {
        synchronized(NSWorkspace.class) {
            NSDistributedNotificationCenter.defaultCenter().postNotification(
                    NSNotification.notificationWithName("com.apple.DownloadFileFinished", file.getAbsolute())
            );
        }
    }
}
