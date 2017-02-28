package ch.cyberduck.core.transfer.copy;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Objects;

public class ChecksumFilter extends AbstractCopyFilter {
    private static final Logger log = Logger.getLogger(ChecksumFilter.class);

    private Upload upload;

    public ChecksumFilter(final Session<?> source, final Session<?> destination, final Map<Path, Path> files) {
        super(source, destination, files);
        this.upload = destination.getFeature(Upload.class);
    }

    public ChecksumFilter(final Session<?> source, final Session<?> destination, final Map<Path, Path> files, final UploadFilterOptions options) {
        super(source, destination, files, options);
    }

    @Override
    public boolean accept(final Path source, final Local local, final TransferStatus parent) throws BackgroundException {
        final Path target = files.get(source);
        if(source.isFile()) {
            if(parent.isExists()) {
                final PathAttributes attributes = new DefaultAttributesFinderFeature(sourceSession).withCache(sourceCache).find(source);
                final Write.Append append = upload.append(target, attributes.getSize(), destinationCache);
                // Compare source with target attributes
                if(append.size == attributes.getSize()) {
                    if(Checksum.NONE != append.checksum) {
                        if(Objects.equals(attributes.getChecksum(), append.checksum)) {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Skip file %s with checksum %s", source, append.checksum));
                            }
                            return false;
                        }
                        log.warn(String.format("Checksum mismatch for %s and %s", source, target));
                    }
                    else {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Skip file %s with remote size %d", source, append.size));
                        }
                        // No need to resume completed transfers
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
