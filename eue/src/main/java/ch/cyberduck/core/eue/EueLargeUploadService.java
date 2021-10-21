package ch.cyberduck.core.eue;

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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.UploadType;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.SegmentRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpResponseException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EueLargeUploadService extends HttpUploadFeature<EueUploadHelper.UploadResponse, MessageDigest> {
    private static final Logger log = Logger.getLogger(EueLargeUploadService.class);

    public static final String RESOURCE_ID = "resourceId";

    private final EueSession session;
    private final Long chunkSize;
    private final Integer concurrency;
    private final EueResourceIdProvider fileid;

    /**
     * Checksums for uploaded segments
     */
    private final List<MessageDigestHolder> checksums = new ArrayList<>();

    private Write<EueUploadHelper.UploadResponse> writer;

    public EueLargeUploadService(final EueSession session, final EueResourceIdProvider fileid, final Write<EueUploadHelper.UploadResponse> writer) {
        this(session, fileid, writer, PreferencesFactory.get().getLong("eue.upload.multipart.size"), PreferencesFactory.get().getInteger("eue.upload.multipart.concurrency"));
    }

    public EueLargeUploadService(final EueSession session, final EueResourceIdProvider fileid, final Write<EueUploadHelper.UploadResponse> writer, final Long chunkSize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.writer = writer;
        this.chunkSize = chunkSize;
        this.concurrency = concurrency;
        this.fileid = fileid;
    }

    @Override
    public EueUploadHelper.UploadResponse upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                                 final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("multipart", concurrency);
        try {
            // Full size of file
            final long size = status.getLength() + status.getOffset();
            final List<Future<EueUploadHelper.UploadResponse>> parts = new ArrayList<>();
            long offset = 0;
            long remaining = status.getLength();
            final String resourceId;
            final String uploadUri;
            if(status.isExists()) {
                resourceId = fileid.getFileId(file, new DisabledListProgressListener());
                uploadUri = EueUploadHelper.updateResource(session, resourceId, UploadType.CHUNKED).getUploadURI();
            }
            else {
                final ResourceCreationResponseEntry uploadResourceCreationResponseEntry = EueUploadHelper.
                        createResource(session, fileid.getFileId(file.getParent(), new DisabledListProgressListener()), file.getName(),
                                status, UploadType.CHUNKED);
                  resourceId = EueResourceIdProvider.getResourceIdFromResourceUri(uploadResourceCreationResponseEntry.getHeaders().getLocation());
                  uploadUri = uploadResourceCreationResponseEntry.getEntity().getUploadURI();
            }
            while(remaining > 0) {
                String offsetIncludedUploadUri = String.format("%s&x_offset=%d", uploadUri, offset);
                final long length = Math.min(chunkSize, remaining);
                parts.add(this.submit(pool, file, local, throttle, listener, status,
                        offsetIncludedUploadUri, resourceId, offset, length, callback));
                remaining -= length;
                offset += length;
            }
            for(Future<EueUploadHelper.UploadResponse> uploadResponseFuture : parts) {
                try {
                    uploadResponseFuture.get();
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
            MessageDigest messageDigest = this.digest();
            checksums.stream().sorted(Comparator.comparing(MessageDigestHolder::getOffset)).forEach(holder -> {
                messageDigest.update(holder.digest.digest());
                messageDigest.update(EueCdash64Compute.intToBytes(Long.valueOf(holder.length).intValue()));
            });
            final EueUploadHelper.UploadResponse completedUploadResponse = new EueMultipartUploadCompleter(session)
                    .getCompletedUploadResponse(uploadUri, size, Base64.encodeBase64URLSafeString(messageDigest.digest()));
            status.setComplete();
            return completedUploadResponse;
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
        finally {
            // Cancel future tasks
            pool.shutdown(false);
        }
    }

    @Override
    protected MessageDigest digest() throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    protected InputStream decorate(final InputStream in, final MessageDigest digest) throws IOException {
        return new DigestInputStream(super.decorate(in, digest), digest);
    }

    public EueUploadHelper.UploadResponse upload(final Path file, final Local local, final BandwidthThrottle throttle,
                                                 final StreamListener listener, final TransferStatus status,
                                                 final StreamCancelation cancel, final StreamProgress progress, final ConnectionCallback callback) throws BackgroundException {
        try {
            final MessageDigest digest = this.digest();
            final EueUploadHelper.UploadResponse uploadResponse = this.transfer(file, local, throttle, listener, status, cancel, progress, callback, digest);
            MessageDigestHolder messageDigestHolder = new MessageDigestHolder(status.getOffset(), status.getLength(), digest);
            checksums.add(messageDigestHolder);
            return uploadResponse;
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    private Future<EueUploadHelper.UploadResponse> submit(final ThreadPool pool, final Path file, final Local local,
                                                          final BandwidthThrottle throttle, final StreamListener listener,
                                                          final TransferStatus overall, final String url, final String resourceId,
                                                          final long offset, final long length, final ConnectionCallback callback) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit %s to queue with offset %d and length %d", file, offset, length));
        }
        final BytecountStreamListener counter = new BytecountStreamListener(listener);
        return pool.execute(new SegmentRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<EueUploadHelper.UploadResponse>() {
            @Override
            public EueUploadHelper.UploadResponse call() throws BackgroundException {
                overall.validate();
                final Map<String, String> parameters = new HashMap<>();
                parameters.put(RESOURCE_ID, resourceId);
                final TransferStatus status = new TransferStatus()
                        .segment(true)
                        .withLength(length)
                        .withParameters(parameters)
                        .withOffset(offset);
                status.setChecksum(writer.checksum(file, status).compute(local.getInputStream(), status));
                status.setUrl(url);
                status.setHeader(overall.getHeader());
                status.setNonces(overall.getNonces());
                final EueUploadHelper.UploadResponse uploadResponse = EueLargeUploadService.this.upload(
                        file, local, throttle, listener, status, overall, status, callback);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Received response for offset number %d", offset));
                }
                return uploadResponse;
            }
        }, overall, counter));
    }

    @Override
    public Upload<EueUploadHelper.UploadResponse> withWriter(final Write<EueUploadHelper.UploadResponse> writer) {
        this.writer = writer;
        return super.withWriter(writer);
    }

    private static class MessageDigestHolder {
        private final long offset;
        private final long length;
        private final MessageDigest digest;

        MessageDigestHolder(final long offset, final long length, final MessageDigest digest) {
            this.offset = offset;
            this.length = length;
            this.digest = digest;
        }

        long getOffset() {
            return offset;
        }
    }
}
