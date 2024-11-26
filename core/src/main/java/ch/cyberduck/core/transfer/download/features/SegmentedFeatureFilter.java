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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.AutoTransferConnectionLimiter;
import ch.cyberduck.core.transfer.FeatureFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class SegmentedFeatureFilter implements FeatureFilter {
    private static final Logger log = LogManager.getLogger(SegmentedFeatureFilter.class);

    private final Session<?> session;

    public SegmentedFeatureFilter(final Session<?> session) {
        this.session = session;
    }

    @Override
    public TransferStatus prepare(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(!session.getFeature(Read.class).offset(file)) {
            log.warn("Reading with offsets not supported for {}", file);
        }
        else {
            if(file.isFile()) {
                if(local.isPresent()) {
                    // Free space on disk
                    long space = 0L;
                    try {
                        space = Files.getFileStore(Paths.get(local.get().getParent().getAbsolute())).getUsableSpace();
                    }
                    catch(IOException e) {
                        log.warn("Failure to determine disk space for {}", file.getParent());
                    }
                    long threshold = new HostPreferences(session.getHost()).getLong("queue.download.segments.threshold");
                    if(status.getLength() * 2 > space) {
                        log.warn("Insufficient free disk space {} for segmented download of {}", space, file);
                    }
                    else if(status.getLength() > threshold) {
                        // if file is smaller than threshold do not attempt to segment
                        final long segmentSize = findSegmentSize(status.getLength(),
                                new AutoTransferConnectionLimiter().getLimit(session.getHost()), threshold,
                                new HostPreferences(session.getHost()).getLong("queue.download.segments.size"),
                                new HostPreferences(session.getHost()).getLong("queue.download.segments.count"));

                        // with default settings this can handle files up to 16 GiB, with 128 segments at 128 MiB.
                        // this scales down to files of size 20MiB with 2 segments at 10 MiB
                        long remaining = status.getLength(), offset = 0;
                        // Sorted list
                        final List<TransferStatus> segments = new ArrayList<>();
                        final Local segmentsFolder = LocalFactory.get(local.get().getParent(), String.format("%s.cyberducksegment", local.get().getName()));
                        for(int segmentNumber = 1; remaining > 0; segmentNumber++) {
                            final Local segmentFile = LocalFactory.get(
                                    segmentsFolder, String.format("%d.cyberducksegment", segmentNumber));
                            // Last part can be less than 5 MB. Adjust part size.
                            long length = Math.min(segmentSize, remaining);
                            final TransferStatus segmentStatus = new TransferStatus()
                                    .segment(true) // Skip completion filter for single segment
                                    .append(true) // Read with offset
                                    .withOffset(offset)
                                    .withLength(length)
                                    .withRename(segmentFile);
                            log.debug("Adding status {} for segment {}", segmentStatus, segmentFile);
                            segments.add(segmentStatus);
                            remaining -= length;
                            offset += length;
                        }
                        status.withSegments(segments);
                    }
                }
            }
        }
        return status;
    }

    @Override
    public void complete(final Path file, final Optional<Local> local, final TransferStatus status, final ProgressListener progress) throws BackgroundException {
        if(status.isSegmented()) {
            if(local.isPresent()) {
                // Obtain ordered list of segments to reassemble
                final List<TransferStatus> segments = status.getSegments();
                log.info("Compile {} segments to file {}", segments.size(), local);
                if(local.get().exists()) {
                    local.get().delete();
                }
                for(Iterator<TransferStatus> iterator = segments.iterator(); iterator.hasNext(); ) {
                    final TransferStatus segmentStatus = iterator.next();
                    // Segment
                    final Local segmentFile = segmentStatus.getRename().local;
                    log.info("Append segment {} to {}", segmentFile, local);
                    segmentFile.copy(local.get(), new Local.CopyOptions().append(true));
                    log.info("Delete segment {}", segmentFile);
                    segmentFile.delete();
                    if(!iterator.hasNext()) {
                        final Local folder = segmentFile.getParent();
                        log.info("Remove segment folder {}", folder);
                        folder.delete();
                    }
                }
            }
        }
    }

    public static long findSegmentSize(final long length, final int initialSplit, final long segmentThreshold, final long segmentSizeMaximum, final long segmentCountLimit) {
        // Make segments
        long parts, segmentSize, nextParts = initialSplit;
        // find segment size
        // starting with part count of queue.connections.limit
        // but not more than queue.download.segments.count
        // or until smaller than queue.download.segments.threshold
        do {
            parts = nextParts;
            nextParts = Math.min(nextParts * 2, segmentCountLimit);
            // round up to next byte
            segmentSize = (length + 1) / parts;
        }
        while(segmentSize > segmentThreshold && parts < segmentCountLimit);
        // round to next divisible by 2
        segmentSize = (segmentSize * 2 + 1) / 2;
        // if larger than maximum segment size
        if(segmentSize > segmentSizeMaximum) {
            // double segment size until parts smaller than queue.download.segments.count
            long nextSize = segmentSizeMaximum;
            do {
                segmentSize = nextSize;
                nextSize *= 2;
                parts = length / segmentSize;
            }
            while(parts > segmentCountLimit);
        }
        return segmentSize;
    }
}
