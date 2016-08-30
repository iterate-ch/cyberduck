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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferItem;

import org.apache.log4j.Logger;

import java.util.List;

public class UploadSymlinkResolver extends AbstractSymlinkResolver<Local> {
    private static final Logger log = Logger.getLogger(UploadSymlinkResolver.class);

    private Symlink feature;

    private List<TransferItem> files;

    public UploadSymlinkResolver(final Symlink feature, final List<TransferItem> files) {
        this.feature = feature;
        this.files = files;
    }

    @Override
    public boolean resolve(final Local file) throws NotfoundException, LocalAccessDeniedException {
        if(PreferencesFactory.get().getBoolean("local.symboliclink.resolve")) {
            // Follow links instead
            return false;
        }
        // Create symbolic link only if supported by the host
        if(feature != null) {
            final Local target = file.getSymlinkTarget();
            // Only create symbolic link if target is included in the upload
            for(TransferItem root : files) {
                if(this.findTarget(target, root.local)) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Resolved target %s for %s", target, file));
                    }
                    return true;
                }
            }
        }
        return false; //Follow links instead
    }

    private boolean findTarget(final Local target, final Local root) {
        return target.equals(root) || target.isChild(root);
    }
}