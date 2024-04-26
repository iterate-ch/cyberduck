package ch.cyberduck.core.transfer.upload;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResumeFilter extends AbstractUploadFilter {
    private static final Logger log = LogManager.getLogger(ResumeFilter.class);

    private final Upload<?> upload;

    public ResumeFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session) {
        this(symlinkResolver, session, new UploadFilterOptions(session.getHost()));
    }

    public ResumeFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session,
                        final UploadFilterOptions options) {
        this(symlinkResolver, session, options, session.getFeature(Upload.class));
    }

    public ResumeFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session,
                        final UploadFilterOptions options, final Upload<?> upload) {
        super(symlinkResolver, session, options);
        this.upload = upload;
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        if(super.accept(file, local, parent)) {
            if(local.isFile()) {
                if(parent.isExists()) {
                    if(find.find(file)) {
                        final PathAttributes attributes = attribute.find(file);
                        if(attributes.getSize() == local.attributes().getSize()) {
                            if(Checksum.NONE != attributes.getChecksum()) {
                                final ChecksumCompute compute = ChecksumComputeFactory.get(attributes.getChecksum().algorithm);
                                if(compute.compute(local.getInputStream(), parent).equals(attributes.getChecksum())) {
                                    if(log.isInfoEnabled()) {
                                        log.info(String.format("Skip file %s with checksum %s", file, attributes.getChecksum()));
                                    }
                                    return false;
                                }
                                log.warn(String.format("Checksum mismatch for %s and %s", file, local));
                            }
                            else {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Skip file %s with remote size %d", file, attributes.getSize()));
                                }
                                // No need to resume completed transfers
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        final TransferStatus status = super.prepare(file, local, parent, progress);
        if(file.isFile()) {
            final Write.Append append = upload.append(file, status);
            if(append.append && append.offset <= status.getLength()) {
                // Append to existing file
                status.withRename((Path) null).withDisplayname((Path) null).setAppend(true);
                status.setLength(status.getLength() - append.offset);
                status.setOffset(append.offset);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Resume file %s at offset %d and remaining length %d", file, status.getOffset(), status.getLength()));
                }
            }
        }
        return status;
    }
}
