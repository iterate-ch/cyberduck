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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Optional;

public class ChecksumFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(ChecksumFeatureFilter.class);

    private final Session<?> session;

    public ChecksumFeatureFilter(final Session<?> session) {
        this.session = session;
    }

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(local.isPresent()) {
            if(file.isFile()) {
                final ChecksumCompute feature = session.getFeature(Write.class).checksum(file, status);
                if(feature != null) {
                    progress.message(MessageFormat.format(LocaleFactory.localizedString("Calculate checksum for {0}", "Status"),
                            file.getName()));
                    try {
                        status.setChecksum(feature.compute(local.get().getInputStream(), status));
                    }
                    catch(LocalAccessDeniedException e) {
                        // Ignore failure reading file when in sandbox when we miss a security scoped access bookmark.
                        // Lock for files is obtained only later in Transfer#pre
                        log.warn(e.getMessage());
                    }
                }
            }
        }
        return status;
    }
}
