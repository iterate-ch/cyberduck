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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

/**
 * @author Joel Wright <joel.wright@sohonet.com>
 * @version $Id$
 */
public class SwiftLargeObjectUploadFeature extends HttpUploadFeature<StorageObject, MessageDigest> {
    private static final Logger log = Logger.getLogger(SwiftLargeObjectUploadFeature.class);

    private SwiftSession session;

    /**
     * At any point, at most <tt>nThreads</tt> threads will be active processing tasks. Possibility of
     * parallel uploads of the segments.
     */
    private ThreadPool pool;

    private PathContainerService containerService
            = new SwiftPathContainerService();

    private Long segmentSize;

    private SwiftSegmentService segmentService;

    private SwiftObjectListService listService;

    public SwiftLargeObjectUploadFeature(final SwiftSession session, final Long segmentSize) {
        this(session, new SwiftObjectListService(session), new SwiftSegmentService(session), new SwiftWriteFeature(session),
                segmentSize, Math.min(Preferences.instance().getInteger("queue.maxtransfers"), Preferences.instance().getInteger("openstack.upload.largeobject.concurrency")));
    }

    public SwiftLargeObjectUploadFeature(final SwiftSession session,
                                         final SwiftObjectListService listService,
                                         final SwiftSegmentService segmentService,
                                         final AbstractHttpWriteFeature<StorageObject> writer,
                                         final Long segmentSize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.pool = new ThreadPool(concurrency, "multipart");
        this.segmentSize = segmentSize;
        this.segmentService = segmentService;
        this.listService = listService;
    }

    @Override
    public StorageObject upload(final Path file, final Local local,
                                final BandwidthThrottle throttle,
                                final StreamListener listener,
                                final TransferStatus status,
                                final LoginCallback callback) throws BackgroundException {
        final List<Path> existingSegments = new ArrayList<Path>();
        if(status.isAppend()) {
            // Get a lexicographically ordered list of the existing file segments
            existingSegments.addAll(listService.list(
                    new Path(containerService.getContainer(file),
                            segmentService.basename(file, status.getLength()), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()));
        }
        // Get the results of the uploads in the order they were submitted
        // this is important for building the manifest, and is not a problem in terms of performance
        // because we should only continue when all segments have uploaded successfully
        final List<StorageObject> completed = new ArrayList<StorageObject>();
        // Submit file segments for concurrent upload
        final List<Future<StorageObject>> segments = new ArrayList<Future<StorageObject>>();
        long remaining = status.getLength();
        long offset = 0;
        for(int segmentNumber = 1; remaining > 0; segmentNumber++) {
            // Segment name with left padded segment number
            final Path segment = new Path(containerService.getContainer(file),
                    segmentService.name(file, status.getLength(), segmentNumber), EnumSet.of(Path.Type.file));
            final Long length = Math.min(segmentSize, remaining);
            if(existingSegments.contains(segment)) {
                final Path existingSegment = existingSegments.get(existingSegments.indexOf(segment));
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Skip segment %s", existingSegment));
                }
                final StorageObject stored = new StorageObject(containerService.getKey(segment));
                stored.setMd5sum(existingSegment.attributes().getChecksum());
                stored.setSize(existingSegment.attributes().getSize());
                completed.add(stored);
            }
            else {
                // Submit to queue
                segments.add(this.submit(segment, local, throttle, listener, status, offset, length));
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Segment %s submitted with size %d and offset %d",
                            segment, length, offset));
                }
            }
            offset += length;
            remaining -= length;
        }
        try {
            for(Future<StorageObject> futureSegment : segments) {
                completed.add(futureSegment.get());
            }
        }
        catch(InterruptedException e) {
            log.error("Part upload failed with interrupt failure");
            throw new ConnectionCanceledException(e);
        }
        catch(ExecutionException e) {
            log.warn(String.format("Part upload failed with execution failure %s", e.getMessage()));
            if(e.getCause() instanceof BackgroundException) {
                throw (BackgroundException) e.getCause();
            }
            throw new BackgroundException(e);
        }
        finally {
            pool.shutdown();
        }
        // Create and upload the large object manifest. It is best to upload all the segments first and
        // then create or update the manifest.
        try {
            // Static Large Object.
            final String manifest = segmentService.manifest(containerService.getContainer(file).getName(), completed);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Creating SLO manifest %s for %s", manifest, file));
            }
            final StorageObject stored = new StorageObject(manifest);
            final String checksum = session.getClient().createSLOManifestObject(new SwiftRegionService(session).lookup(
                    containerService.getContainer(file)),
                    containerService.getContainer(file).getName(),
                    status.getMime(),
                    containerService.getKey(file), manifest, Collections.<String, String>emptyMap());
            stored.setMd5sum(checksum);
            return stored;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Upload failed", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
        }
    }

    private Future<StorageObject> submit(final Path segment, final Local local,
                                         final BandwidthThrottle throttle, final StreamListener listener,
                                         final TransferStatus overall, final Long offset, final Long length) {
        return pool.execute(new Callable<StorageObject>() {
            @Override
            public StorageObject call() throws BackgroundException {
                return SwiftLargeObjectUploadFeature.super.upload(
                        segment, local, throttle, listener, new TransferStatus().length(length).current(offset), overall, overall);
            }
        });
    }

    @Override
    protected InputStream decorate(final InputStream in, final MessageDigest digest) throws IOException {
        if(null == digest) {
            log.warn("MD5 calculation disabled");
            return in;
        }
        else {
            return new DigestInputStream(super.decorate(in, digest), digest);
        }
    }

    @Override
    protected MessageDigest digest() {
        MessageDigest digest = null;
        if(Preferences.instance().getBoolean("openstack.upload.md5")) {
            try {
                digest = MessageDigest.getInstance("MD5");
            }
            catch(NoSuchAlgorithmException e) {
                log.error(e.getMessage());
            }
        }
        return digest;
    }

    @Override
    protected void post(final MessageDigest digest, final StorageObject response) throws BackgroundException {
        if(null != digest) {
            // Obtain locally-calculated MD5 hash.
            final String expected = Hex.encodeHexString(digest.digest());
            // Compare our locally-calculated hash with the ETag returned by S3.
            if(!expected.equals(response.getMd5sum())) {
                throw new ChecksumException("Upload failed",
                        String.format("Mismatch between MD5 hash of uploaded data (%s) and ETag returned by the server (%s)", expected, response.getMd5sum()));
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Verified checksum for %s", response));
            }
        }
    }
}
