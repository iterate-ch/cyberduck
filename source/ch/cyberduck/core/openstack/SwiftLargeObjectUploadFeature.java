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
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Region;
import ch.iterate.openstack.swift.model.StorageObject;

/**
 * @author Joel Wright <joel.wright@sohonet.com>
 * @version $Id$
 */
public class SwiftLargeObjectUploadFeature implements Upload {
    private static final Logger log = Logger.getLogger(SwiftLargeObjectUploadFeature.class);

    private SwiftSession session;

    /**
     * At any point, at most <tt>nThreads</tt> threads will be active processing tasks. Possibility of
     * parallel uploads of the segments.
     */
    private ThreadPool pool = new ThreadPool(
            Preferences.instance().getInteger("openstack.upload.largeobject.concurrency"), "multipart");

    private PathContainerService containerService
            = new PathContainerService();

    private MimeTypeService mapping
            = new MappingMimeTypeService();

    private Long segmentSize;

    private SwiftSegmentService segmentService;

    private SwiftObjectListService listService;

    public SwiftLargeObjectUploadFeature(final SwiftSession session, final Long segmentSize) {
        this(session, new SwiftObjectListService(session), new SwiftSegmentService(session), segmentSize);
    }

    public SwiftLargeObjectUploadFeature(final SwiftSession session,
                                         final SwiftObjectListService listService,
                                         final SwiftSegmentService segmentService,
                                         final Long segmentSize) {
        this.session = session;
        this.segmentSize = segmentSize;
        this.segmentService = segmentService;
        this.listService = listService;
    }

    @Override
    public void upload(final Path file, final Local local,
                       final BandwidthThrottle throttle,
                       final StreamListener listener,
                       final TransferStatus status) throws BackgroundException {

        final Region region = session.getRegion(containerService.getContainer(file));
        final String name = containerService.getKey(file);

        final List<Path> existingSegments = new ArrayList<Path>();
        if(status.isAppend()) {
            // Get a lexicographically ordered list of the existing file segments
            existingSegments.addAll(listService.list(
                    new Path(containerService.getContainer(file),
                            segmentService.basename(file, status.getLength()), Path.DIRECTORY_TYPE), new DisabledListProgressListener()));
        }
        // Get the results of the uploads in the order they were submitted
        // this is important for building the manifest, and is not a problem in terms of performance
        // because we should only continue when all segments have uploaded successfully
        final List<StorageObject> completedSegments = new ArrayList<StorageObject>();
        // Submit file segments for concurrent upload
        final List<Future<StorageObject>> futureSegments = new ArrayList<Future<StorageObject>>();
        long remaining = status.getLength();
        long offset = 0;
        for(int segmentNumber = 1; remaining > 0; segmentNumber++) {
            // Segment name with left padded segment number
            final Path segment = new Path(containerService.getContainer(file),
                    segmentService.name(file, status.getLength(), segmentNumber), Path.FILE_TYPE);
            boolean skip = false;
            if(segmentNumber - 1 < existingSegments.size()) {
                final Path existingSegment = existingSegments.get(segmentNumber - 1);
                if(containerService.getKey(existingSegment).equals(segmentService.name(file, status.getLength(), segmentNumber))) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Skip segment %s", existingSegment));
                    }
                    final StorageObject stored = new StorageObject(containerService.getKey(segment));
                    stored.setMd5sum(existingSegment.attributes().getChecksum());
                    stored.setSize(existingSegment.attributes().getSize());
                    completedSegments.add(stored);
                    skip = true;
                }
            }
            final Long length = Math.min(segmentSize, remaining);
            if(!skip) {
                final Future<StorageObject> futureSegment = this.submitSegment(segment, local,
                        throttle, listener, status, offset, length);
                futureSegments.add(futureSegment);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Segment %s submitted with size %d and offset %d",
                            segment, length, offset));
                }
            }
            offset += length;
            remaining -= length;
        }
        try {
            for(Future<StorageObject> futureSegment : futureSegments) {
                completedSegments.add(futureSegment.get());
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
            final String manifest = segmentService.manifest(containerService.getContainer(file).getName(), completedSegments);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Creating SLO manifest %s for %s", manifest, file));
            }
            session.getClient().createSLOManifestObject(region, containerService.getContainer(file).getName(),
                    mapping.getMime(file.getName()), name, manifest, Collections.<String, String>emptyMap());
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Upload failed", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
        }
    }

    private Future<StorageObject> submitSegment(final Path segment, final Local local,
                                                final BandwidthThrottle throttle, final StreamListener listener,
                                                final TransferStatus status, final Long offset, final Long length) {
        return pool.execute(new Callable<StorageObject>() {
            @Override
            public StorageObject call() throws BackgroundException {
                InputStream in = null;
                ResponseOutputStream<String> out = null;
                final String etag;
                try {
                    in = local.getInputStream();
                    out = new SwiftWriteFeature(session).write(segment, length);
                    new StreamCopier(status).transfer(in, offset, new ThrottledOutputStream(out, throttle), listener, length);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
                finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
                etag = out.getResponse();
                // Maybe we should check the md5sum at some point
                final StorageObject stored = new StorageObject(containerService.getKey(segment));
                stored.setMd5sum(etag);
                stored.setSize(length);
                return stored;
            }
        });
    }
}
