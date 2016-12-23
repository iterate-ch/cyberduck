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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

public class ResumeFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(ResumeFilter.class);

    private final Upload upload;

    private PathCache cache = PathCache.empty();

    public ResumeFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session) {
        this(symlinkResolver, session, new UploadFilterOptions());
    }

    public ResumeFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session,
                        final UploadFilterOptions options) {
        this(symlinkResolver, session, options, session.getFeature(Upload.class));
    }

    public ResumeFilter(final SymlinkResolver<Local> symlinkResolver, final Session<?> session,
                        final UploadFilterOptions options, final Upload upload) {
        super(symlinkResolver, session, options);
        this.upload = upload;
    }

    @Override
    public AbstractUploadFilter withCache(final PathCache cache) {
        this.cache = cache;
        return super.withCache(cache);
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        if(super.accept(file, local, parent)) {
            if(local.isFile()) {
                if(parent.isExists()) {
                    final Write.Append append = upload.append(file, local.attributes().getSize(), cache);
                    if(append.size == local.attributes().getSize()) {
                        if(append.checksum != null) {
                            final ChecksumCompute compute = ChecksumComputeFactory.get(append.checksum.algorithm);
                            if(compute.compute(file, local.getInputStream(), parent).equals(append.checksum)) {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Skip file %s with checksum %s", file, local.attributes().getChecksum()));
                                }
                                return false;
                            }
                            log.warn(String.format("Checksum mismatch for %s and %s", file, local));
                        }
                        else {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Skip file %s with remote size %d", file, append.size));
                            }
                            // No need to resume completed transfers
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        final TransferStatus status = super.prepare(file, local, parent);
        if(local.isFile()) {
            if(parent.isExists()) {
                final Write.Append append = upload.append(file, status.getLength(), cache);
                if(append.append && append.size < local.attributes().getSize()) {
                    // Append to existing file
                    status.setAppend(true);
                    status.setLength(status.getLength() - append.size);
                    status.setOffset(append.size);
                    // Disable use of temporary target when resuming upload
                    status.rename((Path) null);
                }
            }
        }
        return status;
    }
}