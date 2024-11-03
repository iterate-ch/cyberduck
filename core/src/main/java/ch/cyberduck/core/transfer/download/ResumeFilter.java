package ch.cyberduck.core.transfer.download;

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
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResumeFilter extends AbstractDownloadFilter {
    private static final Logger log = LogManager.getLogger(ResumeFilter.class);

    private final Download download;

    public ResumeFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session) {
        this(symlinkResolver, session, new DownloadFilterOptions(session.getHost()));
    }

    public ResumeFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session,
                        final DownloadFilterOptions options) {
        this(symlinkResolver, session, options, session.getFeature(Download.class));
    }

    public ResumeFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session,
                        final DownloadFilterOptions options, final Download download) {
        super(symlinkResolver, session, options);
        this.download = download;
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        if(local.isFile()) {
            if(local.exists()) {
                // Read remote attributes
                final PathAttributes attributes = attribute.find(file);
                if(local.attributes().getSize() == attributes.getSize()) {
                    if(Checksum.NONE != attributes.getChecksum()) {
                        final ChecksumCompute compute = ChecksumComputeFactory.get(attributes.getChecksum().algorithm);
                        if(compute.compute(local.getInputStream(), parent).equals(attributes.getChecksum())) {
                            log.info("Skip file {} with checksum {}", file, attributes.getChecksum());
                            return false;
                        }
                        else {
                            log.warn("Checksum mismatch for {} and {}", file, local);
                        }
                    }
                    else {
                        log.info("Skip file {} with local size {}", file, local.attributes().getSize());
                        // No need to resume completed transfers
                        return false;
                    }
                }
            }
        }
        return super.accept(file, local, parent);
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent, final ProgressListener progress) throws BackgroundException {
        final TransferStatus status = super.prepare(file, local, parent, progress);
        if(status.isSegmented()) {
            for(TransferStatus segmentStatus : status.getSegments()) {
                final Local segmentFile = segmentStatus.getRename().local;
                if(segmentFile.exists()) {
                    log.info("Determine if part {} can be skipped", segmentStatus);
                    if(segmentFile.attributes().getSize() == segmentStatus.getLength()) {
                        segmentStatus.setComplete();
                        status.setLength(status.getLength() - segmentStatus.getLength());
                        status.setOffset(status.getOffset() + segmentStatus.getLength());
                    }
                }
            }
        }
        else {
            if(download.offset(file)) {
                if(local.isFile()) {
                    if(local.exists()) {
                        if(local.attributes().getSize() > 0) {
                            status.setAppend(true);
                            status.setLength(status.getLength() - local.attributes().getSize());
                            status.setOffset(status.getOffset() + local.attributes().getSize());
                            status.withRename((Local) null);
                            if(status.getLength() == 0L) {
                                status.setComplete();
                            }
                        }
                    }
                }
            }
        }
        return status;
    }
}
