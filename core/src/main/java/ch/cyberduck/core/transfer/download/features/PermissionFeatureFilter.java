package ch.cyberduck.core.transfer.download.features;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class PermissionFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(PermissionFeatureFilter.class);

    private final Session<?> session;

    public PermissionFeatureFilter(final Session<?> session) {
        this.session = session;
    }

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        Permission permission = Permission.EMPTY;
        if(new HostPreferences(session.getHost()).getBoolean("queue.download.permissions.default")) {
            if(file.isFile()) {
                permission = new Permission(
                        new HostPreferences(session.getHost()).getInteger("queue.download.permissions.file.default"));
            }
            if(file.isDirectory()) {
                permission = new Permission(
                        new HostPreferences(session.getHost()).getInteger("queue.download.permissions.folder.default"));
            }
        }
        else {
            permission = status.getRemote().getPermission();
        }
        return status.withPermission(permission);
    }

    @Override
    public void complete(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(!Permission.EMPTY.equals(status.getPermission())) {
            if(file.isDirectory()) {
                // Make sure we can read & write files to directory created.
                status.getPermission().setUser(status.getPermission().getUser().or(Permission.Action.read).or(Permission.Action.write).or(Permission.Action.execute));
            }
            if(file.isFile()) {
                // Make sure the owner can always read and write.
                status.getPermission().setUser(status.getPermission().getUser().or(Permission.Action.read).or(Permission.Action.write));
            }
            if(local.isPresent()) {
                log.info("Updating permissions of {} to {}", local, status.getPermission());
                try {
                    local.get().attributes().setPermission(status.getPermission());
                }
                catch(AccessDeniedException e) {
                    // Ignore
                    log.warn(e.getMessage());
                }
            }
        }
    }
}
