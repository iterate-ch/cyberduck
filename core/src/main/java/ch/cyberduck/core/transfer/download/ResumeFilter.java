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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

public class ResumeFilter extends AbstractDownloadFilter {
    private static final Logger log = Logger.getLogger(ResumeFilter.class);

    private final Download download;

    private final AttributesFinder attribute;

    public ResumeFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session) {
        this(symlinkResolver, session, new DownloadFilterOptions());
    }

    public ResumeFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session,
                        final DownloadFilterOptions options) {
        this(symlinkResolver, session, options, session.getFeature(Download.class));
    }

    public ResumeFilter(final SymlinkResolver<Path> symlinkResolver, final Session<?> session,
                        final DownloadFilterOptions options, final Download download) {
        super(symlinkResolver, session, options);
        this.download = download;
        this.attribute = session.getFeature(AttributesFinder.class, new DefaultAttributesFinderFeature(session));
    }

    public AbstractDownloadFilter withCache(final PathCache cache) {
        attribute.withCache(cache);
        return super.withCache(cache);
    }

    @Override
    public boolean accept(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        if(local.isFile()) {
            if(local.exists()) {
                // Read remote attributes
                final PathAttributes attributes = attribute.find(file);
                if(local.attributes().getSize() == attributes.getSize()) {
                    if(attributes.getChecksum() != null) {
                        final ChecksumCompute compute = ChecksumComputeFactory.get(attributes.getChecksum().algorithm);
                        if(compute.compute(file, local.getInputStream(), parent).equals(attributes.getChecksum())) {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Skip file %s with checksum %s", file, local.attributes().getChecksum()));
                            }
                            return false;
                        }
                        else {
                            log.warn(String.format("Checksum mismatch for %s and %s", file, local));
                        }
                    }
                    else {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Skip file %s with local size %d", file, local.attributes().getSize()));
                        }
                        // No need to resume completed transfers
                        return false;
                    }
                }
            }
        }
        return super.accept(file, local, parent);
    }

    @Override
    public TransferStatus prepare(final Path file, final Local local, final TransferStatus parent) throws BackgroundException {
        final TransferStatus status = super.prepare(file, local, parent);
        if(status.isSegmented()) {
            for(TransferStatus segmentStatus : status.getSegments()) {
                final Local segmentFile = segmentStatus.getRename().local;
                if(segmentFile.exists()) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Determine if part %s can be skipped", segmentStatus));
                    }
                    if(segmentFile.attributes().getSize() > 0) {
                        segmentStatus.setAppend(true);
                        segmentStatus.setLength(segmentStatus.getLength() - segmentFile.attributes().getSize());
                        segmentStatus.setOffset(segmentStatus.getOffset() + segmentFile.attributes().getSize());
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
                            status.rename((Local) null);
                        }
                    }
                }
            }
        }
        return status;
    }
}