package ch.cyberduck.core.transfer.copy;

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
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

/**
 * @version $Id$
 */
public class CopyTransferFilter implements TransferPathFilter {

    private final Map<Path, Path> files;

    public CopyTransferFilter(final Map<Path, Path> files) {
        this.files = files;
    }

    @Override
    public boolean accept(final Session session, final Path source) throws BackgroundException {
        if(source.attributes().isDirectory()) {
            final Path destination = files.get(source);
            // Do not attempt to create a directory that already exists
            if(destination.exists()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Session session, final Path source) throws BackgroundException {
        final TransferStatus status = new TransferStatus();
        if(source.attributes().isFile()) {
            status.setLength(source.attributes().getSize());
        }
        return status;
    }

    @Override
    public void complete(final Session<?> session, final Path source, final TransferOptions options, final TransferStatus status) throws BackgroundException {
        if(status.isComplete()) {
            final Path destination = files.get(source);
            final UnixPermission unix = destination.getSession().getFeature(UnixPermission.class);
            if(unix != null) {
                if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    Permission permission = source.attributes().getPermission();
                    if(!Permission.EMPTY.equals(permission)) {
                        unix.setUnixPermission(destination, permission);
                    }
                }
            }
            final Timestamp timestamp = destination.getSession().getFeature(Timestamp.class);
            if(timestamp != null) {
                if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                    timestamp.udpate(destination, source.attributes().getCreationDate(),
                            source.attributes().getModificationDate(),
                            source.attributes().getAccessedDate());
                }
            }
        }
    }
}