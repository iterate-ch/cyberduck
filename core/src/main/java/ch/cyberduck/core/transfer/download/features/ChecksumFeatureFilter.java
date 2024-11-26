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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Optional;

public class ChecksumFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(ChecksumFeatureFilter.class);

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        return status.withChecksum(status.getRemote().getChecksum());
    }

    @Override
    public void complete(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(file.isFile()) {
            if(file.getType().contains(Path.Type.decrypted)) {
                log.warn("Skip checksum verification for {} with client side encryption enabled", file);
            }
            else {
                final Checksum checksum = status.getChecksum();
                if(Checksum.NONE != checksum) {
                    if(local.isPresent()) {
                        final ChecksumCompute compute = ChecksumComputeFactory.get(checksum.algorithm);
                        progress.message(MessageFormat.format(LocaleFactory.localizedString("Calculate checksum for {0}", "Status"),
                                file.getName()));
                        final Checksum download = compute.compute(local.get().getInputStream(), new TransferStatus());
                        if(!checksum.equals(download)) {
                            throw new ChecksumException(
                                    MessageFormat.format(LocaleFactory.localizedString("Download {0} failed", "Error"), file.getName()),
                                    MessageFormat.format(LocaleFactory.localizedString("Mismatch between {0} hash {1} of downloaded data and checksum {2} returned by the server", "Error"),
                                            download.algorithm.toString(), download.hash, checksum.hash));
                        }
                    }
                }
            }
        }
    }
}
