package ch.cyberduck.core.transfer.upload.features;

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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Optional;

public class PermissionFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(PermissionFeatureFilter.class);

    private final Session<?> session;

    public PermissionFeatureFilter(final Session<?> session) {
        this.session = session;
    }

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        final UnixPermission feature = session.getFeature(UnixPermission.class);
        if(feature != null) {
            if(status.isExists()) {
                // Already set when reading attributes of file
                status.setPermission(status.getRemote().getPermission());
            }
            else {
                if(new HostPreferences(session.getHost()).getBoolean("queue.upload.permissions.default")) {
                    status.setPermission(feature.getDefault(file.getType()));
                }
                else {
                    if(local.isPresent()) {
                        // Read permissions from local file
                        status.setPermission(local.get().attributes().getPermission());
                    }
                    else {
                        status.setPermission(feature.getDefault(file.getType()));
                    }
                }
            }
        }
        return status;
    }

    @Override
    public void complete(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(!Permission.EMPTY.equals(status.getPermission())) {
            final UnixPermission feature = session.getFeature(UnixPermission.class);
            if(feature != null) {
                try {
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                            file.getName(), status.getPermission()));
                    feature.setUnixPermission(file, status);
                }
                catch(BackgroundException e) {
                    // Ignore
                    log.warn(e.getMessage());
                }
            }
        }
    }
}
