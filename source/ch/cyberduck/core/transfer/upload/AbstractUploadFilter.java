package ch.cyberduck.core.transfer.upload;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class AbstractUploadFilter implements TransferPathFilter {
    private static final Logger log = Logger.getLogger(AbstractUploadFilter.class);

    private SymlinkResolver symlinkResolver;

    public AbstractUploadFilter(final SymlinkResolver symlinkResolver) {
        this.symlinkResolver = symlinkResolver;
    }

    protected boolean exists(final Session<?> session, final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        final Cache cache = session.cache();
        if(!cache.isCached(file.getParent().getReference())) {
            final AttributedList<Path> list = session.list(file.getParent());
            cache.put(file.getParent().getReference(), list);
        }
        return cache.get(file.getParent().getReference()).contains(file.getReference());
    }

    @Override
    public boolean accept(final Session session, final Path file) throws BackgroundException {
        if(!file.getLocal().exists()) {
            // Local file is no more here
            return false;
        }
        if(file.attributes().isDirectory()) {
            // Do not attempt to create a directory that already exists
            if(this.exists(session, file)) {
                return false;
            }
        }
        else if(file.attributes().isFile()) {
            if(file.getLocal().attributes().isSymbolicLink()) {
                if(!symlinkResolver.resolve(file)) {
                    return symlinkResolver.include(file);
                }
            }
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Session<?> session, final Path file) throws BackgroundException {
        final PathAttributes attributes = file.attributes();
        if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
            if(session.getFeature(UnixPermission.class, null) != null) {
                if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                    if(attributes.isFile()) {
                        attributes.setPermission(new Permission(
                                Preferences.instance().getInteger("queue.upload.permissions.file.default")));
                    }
                    else if(attributes.isDirectory()) {
                        attributes.setPermission(new Permission(
                                Preferences.instance().getInteger("queue.upload.permissions.folder.default")));
                    }
                }
                else {
                    // Read permissions from local file
                    attributes.setPermission(file.getLocal().attributes().getPermission());
                }
            }
        }
        final TransferStatus status = new TransferStatus();
        if(attributes.isFile()) {
            if(file.getLocal().attributes().isSymbolicLink()) {
                if(symlinkResolver.resolve(file)) {
                    // No file size increase for symbolic link to be created on the server
                }
                else {
                    // Will resolve the symbolic link when the file is requested.
                    final Local target = file.getLocal().getSymlinkTarget();
                    status.setLength(target.attributes().getSize());
                }
            }
            else {
                // Read file size from filesystem
                status.setLength(file.getLocal().attributes().getSize());
            }
        }
        return status;
    }

    @Override
    public void complete(final Session<?> session, final Path file, final TransferOptions options, final TransferStatus status) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", file.getAbsolute(), status));
        }
        if(status.isComplete()) {
            final UnixPermission unix = session.getFeature(UnixPermission.class, null);
            if(unix != null) {
                if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    final Permission permission = file.attributes().getPermission();
                    if(!Permission.EMPTY.equals(permission)) {
                        unix.setUnixPermission(file, permission);
                    }
                }
            }
            final Timestamp timestamp = session.getFeature(Timestamp.class, null);
            if(timestamp != null) {
                if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                    // Read timestamps from local file
                    final Attributes attributes = file.getLocal().attributes();
                    timestamp.update(file, attributes.getCreationDate(),
                            attributes.getModificationDate(),
                            attributes.getAccessedDate());
                }
            }
        }
    }
}