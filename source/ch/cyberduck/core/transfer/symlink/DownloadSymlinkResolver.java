package ch.cyberduck.core.transfer.symlink;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * @version $Id$
 */
public class DownloadSymlinkResolver extends AbstractSymlinkResolver<Path> {
    private static final Logger log = Logger.getLogger(DownloadSymlinkResolver.class);

    private List<TransferItem> files;

    public DownloadSymlinkResolver(final List<TransferItem> files) {
        this.files = files;
    }

    @Override
    public boolean resolve(final Path file) {
        if(file.attributes().isSymbolicLink()) {
            if(Preferences.instance().getBoolean("path.symboliclink.resolve")) {
                // Resolve links instead
                return false;
            }
            // Create symbolic link only if choosen in the preferences. Otherwise download target file
            final Path target = file.getSymlinkTarget();
            // Only create symbolic link if target is included in the download
            for(TransferItem root : files) {
                if(this.findTarget(target, root.remote)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean include(final Path file) {
        if(file.attributes().isSymbolicLink()) {
            final Path target = file.getSymlinkTarget();
            // Do not transfer files referenced from symlinks pointing to files also included
            for(TransferItem root : files) {
                if(this.findTarget(target, root.remote)) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Skip file %s with target %s already included", file, target));
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findTarget(final Path target, final Path root) {
        return target.equals(root) || target.isChild(root);
    }
}