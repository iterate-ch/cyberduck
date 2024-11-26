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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class TimestampFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(TimestampFeatureFilter.class);

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        return status.withModified(status.getRemote().getModificationDate()).withCreated(status.getRemote().getCreationDate());
    }

    @Override
    public void complete(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(status.getModified() != null) {
            if(local.isPresent()) {
                log.info("Updating timestamp of {} to {}", local, status.getModified());
                try {
                    local.get().attributes().setModificationDate(status.getModified());
                }
                catch(AccessDeniedException e) {
                    // Ignore
                    log.warn(e.getMessage());
                }
            }
        }
    }
}
