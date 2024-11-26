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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class TemporaryFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(TemporaryFeatureFilter.class);

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(local.isPresent()) {
            if(StringUtils.startsWith(status.getRemote().getDisplayname(), "file:")) {
                final String filename = StringUtils.removeStart(status.getRemote().getDisplayname(), "file:");
                if(!StringUtils.equals(file.getName(), filename)) {
                    status.withDisplayname(LocalFactory.get(local.get().getParent(), filename));
                    int no = 0;
                    while(status.getDisplayname().local.exists()) {
                        String proposal = String.format("%s-%d", FilenameUtils.getBaseName(filename), ++no);
                        if(StringUtils.isNotBlank(Path.getExtension(filename))) {
                            proposal += String.format(".%s", Path.getExtension(filename));
                        }
                        status.withDisplayname(LocalFactory.get(local.get().getParent(), proposal));
                    }
                }
            }
        }
        return status;
    }

    @Override
    public void complete(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(file.isFile()) {
            if(status.getDisplayname().local != null) {
                if(local.isPresent()) {
                    log.info("Rename file {} to {}", file, status.getDisplayname().local);
                    local.get().rename(status.getDisplayname().local);
                }
            }
        }
    }
}
