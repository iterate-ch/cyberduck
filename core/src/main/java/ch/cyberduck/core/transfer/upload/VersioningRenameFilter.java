package ch.cyberduck.core.transfer.upload;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VersioningRenameFilter extends AbstractUploadFilter {
    private static final Logger log = LogManager.getLogger(VersioningRenameFilter.class);

    private final Versioning versioning;

    public VersioningRenameFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session, final UploadFilterOptions options) {
        super(symlinkResolver, session, options);
        this.versioning = session.getFeature(Versioning.class);
    }

    @Override
    public void apply(final Path file, final Local local, final TransferStatus status, final ProgressListener listener) throws BackgroundException {
        if(status.isExists()) {
            if(versioning.save(file)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Clear exist flag for file %s", file));
                }
                status.exists(false).getDisplayname().exists(false);
            }
        }
    }
}