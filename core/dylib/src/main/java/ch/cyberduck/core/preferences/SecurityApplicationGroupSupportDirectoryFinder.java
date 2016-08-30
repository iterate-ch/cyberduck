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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.LocalSymlinkFactory;
import ch.cyberduck.core.local.LocalTrashFactory;
import ch.cyberduck.core.local.features.Symlink;
import ch.cyberduck.core.local.features.Trash;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;

import java.io.File;
import java.io.IOException;

public class SecurityApplicationGroupSupportDirectoryFinder implements SupportDirectoryFinder {
    private static final Logger log = Logger.getLogger(SecurityApplicationGroupSupportDirectoryFinder.class);

    private final String identifier;

    public SecurityApplicationGroupSupportDirectoryFinder() {
        this("G69SCX94XU.duck");
    }

    public SecurityApplicationGroupSupportDirectoryFinder(final String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Local find() {
        final NSFileManager manager = NSFileManager.defaultManager();
        if(manager.respondsToSelector(Foundation.selector("containerURLForSecurityApplicationGroupIdentifier:"))) {
            final NSURL group = manager
                    .containerURLForSecurityApplicationGroupIdentifier(identifier);
            if(null == group) {
                log.warn("Missing com.apple.security.application-groups in sandbox entitlements");
            }
            else {
                // You should organize the contents of this directory in the same way that any other Library folder is organized
                final String application = "duck";
                final Local folder = LocalFactory.get(String.format("%s/Library/Application Support", group.path()), application);
                try {
                    // In previous versions of OS X, although the group container directory is part of your sandbox,
                    // the directory itself is not created automatically.
                    if(!folder.exists()) {
                        log.info(String.format("Create shared security application group folder %s", folder));
                        folder.mkdir();
                    }
                    log.info(String.format("Shared security application group folder %s is empty. Attempt to migrate support directory.", folder));
                    final Local previous = new ApplicationSupportDirectoryFinder().find();
                    if(previous.exists()) {
                        log.warn(String.format("Migrate application support folder from %s to %s", previous, folder));
                        // Rename folder recursively
                        try {
                            FileUtils.copyDirectory(new File(previous.getAbsolute()), new File(folder.getAbsolute()));
                            log.warn(String.format("Move application support folder %s to Trash", previous));
                            try {
                                final Trash trash = LocalTrashFactory.get();
                                trash.trash(previous);
                                final Symlink symlink = LocalSymlinkFactory.get();
                                symlink.symlink(previous, folder.getAbsolute());
                            }
                            catch(LocalAccessDeniedException e) {
                                log.warn(String.format("Failure cleaning up previous application support directory. %s", e.getMessage()));
                            }
                        }
                        catch(IOException e) {
                            log.warn(String.format("Failure migrating %s to security application group directory %s. %s", previous, folder, e.getMessage()));
                        }
                    }
                    else {
                        log.debug(String.format("No previous application support folder found in %s", previous));
                    }
                    return folder;
                }
                catch(AccessDeniedException e) {
                    log.warn(String.format("Failure creating security application group directory. %s", e.getMessage()));
                }
            }
        }
        log.warn("Missing support for security application groups. Default to application support directory");
        // Fallback for 10.7 and earlier
        return new ApplicationSupportDirectoryFinder().find();
    }
}
