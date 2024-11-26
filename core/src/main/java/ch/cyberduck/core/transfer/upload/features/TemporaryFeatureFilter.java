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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Optional;

public class TemporaryFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(TemporaryFeatureFilter.class);

    private final Session<?> session;

    public TemporaryFeatureFilter(final Session<?> session) {
        this.session = session;
    }

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(file.isFile()) {
            final Move feature = session.getFeature(Move.class);
            final Path renamed = new Path(file.getParent(),
                    MessageFormat.format(new HostPreferences(session.getHost()).getProperty("queue.upload.file.temporary.format"),
                            file.getName(), new AlphanumericRandomStringService().random()), file.getType());
            if(feature.isSupported(file, renamed)) {
                log.debug("Set temporary filename {}", renamed);
                // Set target name after transfer
                status.withRename(renamed).withDisplayname(file);
                // Remember status of target file for later rename
                status.getDisplayname().exists(status.isExists());
                // Keep exist flag for subclasses to determine additional rename strategy
            }
            else {
                log.warn("Cannot use temporary filename for upload with missing rename support for {}", file);
            }
        }
        return status;
    }

    @Override
    public void apply(final Path file, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(status.getRename().remote != null) {
            log.debug("Clear exist flag for file {}", file);
            // Reset exist flag after subclass has applied strategy
            status.setExists(false);
        }
    }

    @Override
    public void complete(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(file.isFile()) {
            if(status.getDisplayname().remote != null) {
                log.info("Rename file {} to {}", file, status.getDisplayname().remote);
                final Move feature = session.getFeature(Move.class);
                feature.move(file, status.getDisplayname().remote, new TransferStatus(status).exists(status.getDisplayname().exists),
                        new Delete.DisabledCallback(), new DisabledConnectionCallback());
            }
        }
    }
}
