package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.local.LocalSymlinkFactory;
import ch.cyberduck.core.local.LocalTrashFactory;
import ch.cyberduck.core.local.features.Symlink;
import ch.cyberduck.core.local.features.Trash;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class MigratingSupportDirectoryFinder implements SupportDirectoryFinder {
    private static final Logger log = LogManager.getLogger(MigratingSupportDirectoryFinder.class);

    private final SupportDirectoryFinder deprecated;
    private final SupportDirectoryFinder proxy;

    /**
     * @param deprecated Deprecated implemenation providing previous application support folder
     */
    public MigratingSupportDirectoryFinder(final SupportDirectoryFinder deprecated, final SupportDirectoryFinder proxy) {
        this.deprecated = deprecated;
        this.proxy = proxy;
    }

    @Override
    public SupportDirectoryFinder setup() {
        final Local previous = deprecated.find();
        if(previous.exists()) {
            if(previous.isSymbolicLink()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Previous application support folder %s already symlink", previous));
                }
            }
            else {
                final Local folder = proxy.find().getParent();
                if(log.isWarnEnabled()) {
                    log.warn(String.format("Migrate application support folder from %s to %s", previous, folder));
                }
                // Rename folder recursively
                try {
                    FileUtils.copyDirectory(new File(previous.getAbsolute()), new File(folder.getAbsolute()));
                    try {
                        final Trash trash = LocalTrashFactory.get();
                        if(log.isWarnEnabled()) {
                            log.warn(String.format("Trash previous application support folder %s", previous));
                        }
                        trash.trash(previous);
                        final Symlink symlink = LocalSymlinkFactory.get();
                        symlink.symlink(previous, folder.getAbsolute());
                    }
                    catch(AccessDeniedException e) {
                        log.warn(String.format("Failure %s creating symbolcic link for previous application support directory %s", e, previous));
                    }
                }
                catch(IOException e) {
                    log.warn(String.format("Failure %s migrating %s to security application group directory %s", e, previous, folder));
                }
            }
        }
        else {
            log.debug(String.format("No previous application support folder found in %s", previous));
        }
        return this;
    }

    @Override
    public Local find() {
        return proxy.find();
    }
}
