package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSession;
import ch.cyberduck.core.box.io.swagger.client.model.UploadedPart;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.SegmentRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class BoxLargeUploadService extends HttpUploadFeature<BoxLargeUploadService.BoxUploadResponse, MessageDigest> {
    private static final Logger log = Logger.getLogger(BoxLargeUploadService.class);

    public static final String UPLOAD_SESSION_ID = "uploadSessionId";
    public static final String OVERALL_LENGTH = "overall-length";

    private final BoxSession session;
    private final Long chunksize;
    private final Integer concurrency;
    private final BoxFileidProvider fileid;
    private final BoxApiClient client;

    private Write<BoxUploadResponse> writer;

    public BoxLargeUploadService(final BoxSession session, final BoxFileidProvider fileid, final Write<BoxUploadResponse> writer) {
        this(session, fileid, writer, new HostPreferences(session.getHost()).getLong("box.upload.multipart.size"),
                new HostPreferences(session.getHost()).getInteger("box.upload.multipart.concurrency"));
    }

    public BoxLargeUploadService(final BoxSession session, final BoxFileidProvider fileid, final Write<BoxUploadResponse> writer, final Long chunksize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.writer = writer;
        this.chunksize = chunksize;
        this.concurrency = concurrency;
        this.fileid = fileid;
        this.client = new BoxApiClient(this.session.getClient());
        this.client.setBasePath("https://upload.box.com/api/2.0");
    }

    @Override
    public BoxUploadResponse upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                    final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("multipart", concurrency);
        try {
            final List<Future<BoxUploadResponse>> parts = new ArrayList<>();
            long offset = 0;
            long remaining = status.getLength();
            final UploadSession uploadSession = BoxUploadHelper.getUploadSession(status, client, file, fileid);
            for(int partNumber = 1; remaining > 0; partNumber++) {
                final long length = Math.min(chunksize, remaining);
                parts.add(this.submit(pool, file, local, throttle, listener, status,
                        uploadSession.getId(), partNumber, offset, length, callback));
                remaining -= length;
                offset += length;
            }
            // Checksums for uploaded segments
            final List<BoxUploadResponse> chunks = new ArrayList<>();
            for(Future<BoxUploadResponse> uploadResponseFuture : parts) {
                try {
                    chunks.add(uploadResponseFuture.get());
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
                    throw new BackgroundException(e.getCause());
                }
            }
            final BoxMultipartUploadCommitter boxMultipartUploadCommitter = new BoxMultipartUploadCommitter(session);
            status.setChecksum(writer.checksum(file, status).compute(local.getInputStream(), status));
            final Files files = boxMultipartUploadCommitter.commitUploadSession(file.getName(), client.getBasePath(), uploadSession.getId(), status, chunks.stream().map(BoxUploadResponse::getUploadedPart).map(UploadedPart::getPart).collect(Collectors.toList()));
            final BoxUploadResponse boxUploadResponse = new BoxUploadResponse();
            boxUploadResponse.setComplete(true);
            boxUploadResponse.setFiles(files);
            status.setComplete();
            return boxUploadResponse;
        }
        catch(ApiException | IOException e) {
            throw new BackgroundException(e);
        }
        finally {
            // Cancel future tasks
            pool.shutdown(false);
        }
    }

    private Future<BoxLargeUploadService.BoxUploadResponse> submit(final ThreadPool pool, final Path file, final Local local,
                                                                   final BandwidthThrottle throttle, final StreamListener listener,
                                                                   final TransferStatus overall, final String uploadSessionId, final int partNumber, final long offset, final long length, final ConnectionCallback callback) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit %s to queue with offset %d and length %d", file, offset, length));
        }
        final BytecountStreamListener counter = new BytecountStreamListener(listener);
        return pool.execute(new SegmentRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<BoxUploadResponse>() {
            @Override
            public BoxUploadResponse call() throws BackgroundException {
                overall.validate();
                final TransferStatus status = new TransferStatus()
                        .segment(true)
                        .withOffset(offset)
                        .withLength(length);
                status.setPart(partNumber);
                status.setHeader(overall.getHeader());
                status.setChecksum(writer.checksum(file, status).compute(local.getInputStream(), status));
                final Map<String, String> parameters = new HashMap<>();
                parameters.put(UPLOAD_SESSION_ID, uploadSessionId);
                parameters.put(OVERALL_LENGTH, String.valueOf(overall.getLength()));
                status.withParameters(parameters);
                final BoxUploadResponse boxUploadResponse = BoxLargeUploadService.this.upload(
                        file, local, throttle, listener, status, overall, status, callback);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Received response %s for part %d", boxUploadResponse.getUploadedPart(), partNumber));
                }
                return boxUploadResponse;
            }
        }, overall, counter));
    }

    @Override
    public Upload<BoxUploadResponse> withWriter(final Write<BoxUploadResponse> writer) {
        this.writer = writer;
        return super.withWriter(writer);
    }

    static class BoxUploadResponse {
        private Files files;
        private boolean isComplete;
        private UploadedPart uploadedPart;

        public Files getFiles() {
            return files;
        }

        public void setFiles(final Files files) {
            this.files = files;
        }

        public boolean isComplete() {
            return isComplete;
        }

        public void setComplete(final boolean complete) {
            isComplete = complete;
        }

        public UploadedPart getUploadedPart() {
            return uploadedPart;
        }

        public void setUploadedPart(final UploadedPart uploadedPart) {
            this.uploadedPart = uploadedPart;
        }

    }
}
