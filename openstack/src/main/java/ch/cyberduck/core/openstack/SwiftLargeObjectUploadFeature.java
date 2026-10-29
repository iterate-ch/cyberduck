package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.SegmentRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftLargeObjectUploadFeature extends HttpUploadFeature<StorageObject> {
    private static final Logger log = LogManager.getLogger(SwiftLargeObjectUploadFeature.class);

    private final SwiftSession session;
    private final PathContainerService containerService = new DefaultPathContainerService();
    private final SwiftSegmentService segmentService;
    private final SwiftObjectListService listService;
    private final SwiftRegionService regionService;
    private final Long segmentSize;
    private final Integer concurrency;

    public SwiftLargeObjectUploadFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this(session, regionService, HostPreferencesFactory.get(session.getHost()).getLong("openstack.upload.largeobject.size"),
                HostPreferencesFactory.get(session.getHost()).getInteger("openstack.upload.largeobject.concurrency"));
    }

    public SwiftLargeObjectUploadFeature(final SwiftSession session, final SwiftRegionService regionService,
                                         final Long segmentSize, final Integer concurrency) {
        this(session, regionService, new SwiftObjectListService(session, regionService), new SwiftSegmentService(session, regionService),
                segmentSize, concurrency);
    }

    public SwiftLargeObjectUploadFeature(final SwiftSession session,
                                         final SwiftRegionService regionService,
                                         final SwiftObjectListService listService,
                                         final SwiftSegmentService segmentService,
                                         final Long segmentSize, final Integer concurrency) {
        this.session = session;
        this.regionService = regionService;
        this.segmentSize = segmentSize;
        this.segmentService = segmentService;
        this.listService = listService;
        this.concurrency = concurrency;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        // Algorithm of the checksum of the segments and of the ETag of the manifest calculated from the ETag of the
        // segments. No checksum of the whole file is calculated.
        return ChecksumComputeFactory.get(HashAlgorithm.md5);
    }

    @Override
    public StorageObject upload(final Write<StorageObject> write, final Path file, final Local local,
                                final BandwidthThrottle throttle,
                                final ProgressListener progress, final StreamListener listener,
                                final TransferStatus status,
                                final ConnectionCallback callback) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("multipart", concurrency);
        final List<Path> existingSegments = new ArrayList<>();
        if(status.isAppend()) {
            // Get a lexicographically ordered list of the existing file segments
            try {
                existingSegments.addAll(listService.list(segmentService.getSegmentsDirectory(file), new DisabledListProgressListener()).toList());
            }
            catch(NotfoundException e) {
                // Ignore
            }
        }
        else {
            if(status.isExists()) {
                // Delete existing segments
                if(HostPreferencesFactory.get(session.getHost()).getBoolean("openstack.upload.largeobject.cleanup")) {
                    // Clean up any old segments
                    new SwiftMultipleDeleteFeature(session).delete(
                            new SwiftSegmentService(session, regionService).list(file), callback, new Delete.DisabledCallback());
                }
            }
        }
        // Get the results of the uploads in the order they were submitted
        // this is important for building the manifest, and is not a problem in terms of performance
        // because we should only continue when all segments have uploaded successfully
        final List<StorageObject> completed = new ArrayList<>();
        // MD5 of the segments by name
        final Map<String, Checksum> checksums = new ConcurrentHashMap<>();
        // Submit file segments for concurrent upload
        final List<Future<StorageObject>> segments = new ArrayList<>();
        long remaining = status.getLength();
        long offset = 0;
        for(int segmentNumber = 1; remaining > 0; segmentNumber++) {
            final long length = Math.min(segmentSize, remaining);
            // Segment name with left padded segment number
            final Path segment = segmentService.getSegment(file, segmentNumber);
            if(existingSegments.contains(segment)) {
                final Path existingSegment = existingSegments.get(existingSegments.indexOf(segment));
                log.debug("Skip segment {}", existingSegment);
                final StorageObject stored = new StorageObject(containerService.getKey(segment));
                if(HashAlgorithm.md5.equals(existingSegment.attributes().getChecksum().algorithm)) {
                    stored.setMd5sum(existingSegment.attributes().getChecksum().hash);
                    checksums.put(stored.getName(), existingSegment.attributes().getChecksum());
                }
                stored.setSize(existingSegment.attributes().getSize());
                offset += existingSegment.attributes().getSize();
                completed.add(stored);
            }
            else {
                // Submit to queue
                segments.add(this.submit(pool, write, segment, local, throttle, listener, status, offset, length, checksums, callback));
                log.debug("Segment {} submitted with size {} and offset {}", segment, length, offset);
                remaining -= length;
                offset += length;
            }
        }
        try {
            completed.addAll(Interruptibles.awaitAll(segments));
        }
        finally {
            pool.shutdown(false);
        }
        log.info("Finished large file upload {} with {} parts", file, completed.size());
        // Create and upload the large object manifest. It is best to upload all the segments first and
        // then create or update the manifest.
        try {
            progress.message(MessageFormat.format(LocaleFactory.localizedString("Finalize {0}", "Status"),
                    file.getName()));
            // Static Large Object.
            final String manifest = segmentService.manifest(containerService.getContainer(file).getName(), completed);
            log.debug("Creating SLO manifest {} for {}", manifest, file);
            final StorageObject stored = new StorageObject(containerService.getKey(file));
            stored.setSize(status.getOffset() + status.getLength());
            session.getClient().createSLOManifestObject(regionService.lookup(
                            containerService.getContainer(file)),
                    containerService.getContainer(file).getName(),
                    status.getMime(),
                    containerService.getKey(file), manifest, Collections.emptyMap());
            final PathAttributes attributes;
            if(status.isVerify()) {
                // The ETag returned in the response of creating the manifest is not the ETag of the static large object. The
                // ETag of the manifest in the response for a HEAD is the MD5 of the concatenated ETags of the segments.
                attributes = new SwiftAttributesFinderFeature(session).find(file);
                stored.setMd5sum(attributes.getChecksum().hash);
                // Compared with the checksum returned by the server by the upload filter when the upload is complete
                status.setDigest(composite(completed, checksums));
            }
            else {
                attributes = new SwiftAttributesFinderFeature(session).toAttributes(stored);
            }
            // Mark parent status as complete
            status.setResponse(attributes).setComplete();
            return stored;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    private Future<StorageObject> submit(final ThreadPool pool, final Write<StorageObject> write, final Path segment, final Local local,
                                         final BandwidthThrottle throttle, final StreamListener listener,
                                         final TransferStatus overall, final Long offset, final Long length, final Map<String, Checksum> checksums, final ConnectionCallback callback) throws ConnectionCanceledException {
        overall.validate();
        log.info("Submit part {} to queue with offset {} and length {}", segment, offset, length);
        final BytecountStreamListener counter = new BytecountStreamListener(listener);
        return pool.execute(new SegmentRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<StorageObject>() {
            @Override
            public StorageObject call() throws BackgroundException {
                overall.validate();
                final TransferStatus status = new TransferStatus()
                        .setSegment(true)
                        .setParent(overall)
                        .setLength(length)
                        .setOffset(offset);
                status.setHeader(overall.getHeader());
                // Optional checksum of the segment for the server to verify
                final Checksum md5 = status.compute(local, HashAlgorithm.md5);
                status.setChecksum(md5);
                checksums.put(containerService.getKey(segment), md5);
                return SwiftLargeObjectUploadFeature.this.upload(
                        write, segment, local, throttle, ProgressListener.noop, counter, status, overall, status, callback);
            }
        }, overall, counter));
    }

    /**
     * @param completed Segments in the order of the manifest
     * @param checksums MD5 of the segments calculated prior to the transfer or listed when resuming
     * @return Checksum calculated like the ETag of a static large object, which is the MD5 of the concatenated ETags
     * of the segments
     */
    private static Checksum composite(final List<StorageObject> completed, final Map<String, Checksum> checksums) throws BackgroundException {
        final StringBuilder concatenated = new StringBuilder();
        for(StorageObject segment : completed) {
            final Checksum md5 = checksums.get(segment.getName());
            if(null == md5) {
                log.warn("Missing checksum of segment {}", segment.getName());
                return Checksum.NONE;
            }
            concatenated.append(md5.hash);
        }
        return ChecksumComputeFactory.get(HashAlgorithm.md5).compute(
                new ByteArrayInputStream(concatenated.toString().getBytes(StandardCharsets.UTF_8)), new TransferStatus());
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        final List<Path> segments;
        long size = 0L;
        try {
            segments = new SwiftObjectListService(session, regionService).list(new SwiftSegmentService(session, regionService)
                    .getSegmentsDirectory(file), new DisabledListProgressListener()).toList();
            if(segments.isEmpty()) {
                return Write.override;
            }
        }
        catch(NotfoundException e) {
            return Write.override;
        }
        for(Path segment : segments) {
            size += segment.attributes().getSize();
        }
        return new Write.Append(true).withStatus(status).withOffset(size);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) {
        // The ETag of the manifest is calculated from the ETag of the segments and compared when the upload is complete
        return EnumSet.of(Flags.checksumResponse);
    }
}
