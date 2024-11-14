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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VersioningFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(VersioningFeatureFilter.class);

    private final Session<?> session;

    public VersioningFeatureFilter(final Session<?> session) {
        this.session = session;
    }

    @Override
    public void apply(final Path file, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(file.isFile()) {
            if(status.isExists()) {
                if(status.isAppend()) {
                    // Append to existing file
                    log.debug("Resume upload for existing file {}", file);
                }
                else {
                    switch(session.getHost().getProtocol().getVersioningMode()) {
                        case custom:
                            final Versioning feature = session.getFeature(Versioning.class);
                            if(feature != null) {
                                log.debug("Use custom versioning {}", feature);
                                if(feature.getConfiguration(file).isEnabled()) {
                                    log.debug("Enabled versioning for {}", file);
                                    if(feature.save(file)) {
                                        log.debug("Clear exist flag for file {}", file);
                                        status.setExists(false).getDisplayname().exists(false);
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}
