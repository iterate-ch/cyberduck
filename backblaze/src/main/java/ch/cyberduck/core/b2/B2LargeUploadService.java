package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.threading.DefaultThreadPool;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2FinishLargeFileResponse;
import synapticloop.b2.response.B2UploadPartResponse;
import synapticloop.b2.response.BaseB2Response;

public class B2LargeUploadService extends HttpUploadFeature<BaseB2Response, MessageDigest> {
    private static final Logger log = Logger.getLogger(B2LargeUploadService.class);

    /**
     * The maximum allowed parts in a multipart upload.
     */
    public static final int MAXIMUM_UPLOAD_PARTS = 10000;

    private final PathContainerService containerService
            = new PathContainerService();

    private final B2Session session;

    private final Long partSize;

    private final Integer concurrency;

    private Write<BaseB2Response> writer;

    public B2LargeUploadService(final B2Session session, final Write<BaseB2Response> writer, final Long partSize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.writer = writer;
        this.partSize = partSize;
        this.concurrency = concurrency;
    }

    @Override
    public BaseB2Response upload(final Path file, final Local local,
                                 final BandwidthThrottle throttle,
                                 final StreamListener listener,
                                 final TransferStatus status,
                                 final ConnectionCallback callback) throws BackgroundException {
        final DefaultThreadPool pool = new DefaultThreadPool("largeupload", concurrency);
        try {
            final String fileid;
            // Get the results of the uploads in the order they were submitted
            // this is important for building the manifest, and is not a problem in terms of performance
            // because we should only continue when all segments have uploaded successfully
            final List<B2UploadPartResponse> completed = new ArrayList<B2UploadPartResponse>();
            if(status.isAppend()) {
                // Add already completed parts
                final B2LargeUploadPartService partService = new B2LargeUploadPartService(session);
                final List<B2FileInfoResponse> uploads = partService.find(file);
                if(uploads.isEmpty()) {
                    fileid = session.getClient().startLargeFileUpload(new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                            containerService.getKey(file), status.getMime(), status.getMetadata()).getFileId();
                }
                else {
                    fileid = uploads.iterator().next().getFileId();
                    completed.addAll(partService.list(fileid));
                }
            }
            else {
                fileid = session.getClient().startLargeFileUpload(new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                        containerService.getKey(file), status.getMime(), status.getMetadata()).getFileId();
            }
            // Save file id for use in part referencing this
            file.attributes().setVersionId(fileid);

            // Submit file segments for concurrent upload
            final List<Future<B2UploadPartResponse>> parts = new ArrayList<Future<B2UploadPartResponse>>();
            long remaining = status.getLength();
            long offset = 0;
            for(int partNumber = 1; remaining > 0; partNumber++) {
                boolean skip = false;
                if(status.isAppend()) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Determine if part number %d can be skipped", partNumber));
                    }
                    for(B2UploadPartResponse c : completed) {
                        if(c.getPartNumber().equals(partNumber)) {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Skip completed part number %d", partNumber));
                            }
                            skip = true;
                            final Long length = c.getContentLength();
                            remaining -= length;
                            offset += length;
                            break;
                        }
                    }
                }
                else {
                    final Long length = Math.min(Math.max((status.getLength() + status.getOffset()) / B2LargeUploadService.MAXIMUM_UPLOAD_PARTS, partSize), remaining);
                    // Submit to queue
                    parts.add(this.submit(pool, file, local, throttle, listener, status, partNumber, offset, length, callback));
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Part %s submitted with size %d and offset %d",
                                partNumber, length, offset));
                    }
                    remaining -= length;
                    offset += length;
                }
            }
            try {
                for(Future<B2UploadPartResponse> f : parts) {
                    completed.add(f.get());
                }
            }
            catch(InterruptedException e) {
                log.error("Part upload failed with interrupt failure");
                status.setCanceled();
                throw new ConnectionCanceledException(e);
            }
            catch(ExecutionException e) {
                log.warn(String.format("Part upload failed with execution failure %s", e.getMessage()));
                if(e.getCause() instanceof BackgroundException) {
                    throw (BackgroundException) e.getCause();
                }
                throw new DefaultExceptionMappingService().map(e.getCause());
            }
            finally {
                pool.shutdown(false);
            }
            completed.sort(new Comparator<B2UploadPartResponse>() {
                @Override
                public int compare(final B2UploadPartResponse o1, final B2UploadPartResponse o2) {
                    return o1.getPartNumber().compareTo(o2.getPartNumber());
                }
            });
            final List<String> checksums = new ArrayList<String>();
            for(B2UploadPartResponse part : completed) {
                checksums.add(part.getContentSha1());
            }
            final B2FinishLargeFileResponse response = session.getClient().finishLargeFileUpload(fileid, checksums.toArray(new String[checksums.size()]));
            if(log.isInfoEnabled()) {
                log.info(String.format("Finished large file upload %s with %d parts", file, completed.size()));
            }
            // Mark parent status as complete
            status.setComplete();
            return response;
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(session).map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    private Future<B2UploadPartResponse> submit(final ThreadPool pool, final Path file, final Local local,
                                                final BandwidthThrottle throttle, final StreamListener listener,
                                                final TransferStatus overall,
                                                final int partNumber,
                                                final Long offset, final Long length, final ConnectionCallback callback) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit part %d of %s to queue with offset %d and length %d", partNumber, file, offset, length));
        }
        return pool.execute(new DefaultRetryCallable<B2UploadPartResponse>(new BackgroundExceptionCallable<B2UploadPartResponse>() {
            @Override
            public B2UploadPartResponse call() throws BackgroundException {
                if(overall.isCanceled()) {
                    throw new ConnectionCanceledException();
                }
                final TransferStatus status = new TransferStatus()
                        .length(length)
                        .skip(offset);
                status.setHeader(overall.getHeader());
                status.setNonces(overall.getNonces());
                status.setChecksum(writer.checksum().compute(
                        StreamCopier.skip(new BoundedInputStream(local.getInputStream(), offset + length), offset),
                        status));
                status.setSegment(true);
                status.setPart(partNumber);
                return (B2UploadPartResponse) B2LargeUploadService.super.upload(file, local, throttle, listener, status, overall, new StreamProgress() {
                    @Override
                    public void progress(final long bytes) {
                        status.progress(bytes);
                        // Discard sent bytes in overall progress if there is an error reply for segment.
                        overall.progress(bytes);
                    }

                    @Override
                    public void setComplete() {
                        status.setComplete();
                    }
                }, callback);
            }
        }, overall));
    }

    @Override
    public Upload<BaseB2Response> withWriter(final Write<BaseB2Response> writer) {
        this.writer = writer;
        return super.withWriter(writer);
    }
}
