package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteS3FileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.GeneratePresignedUrlsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.PresignedUrl;
import ch.cyberduck.core.sds.io.swagger.client.model.S3FileUploadPart;
import ch.cyberduck.core.sds.io.swagger.client.model.S3FileUploadStatus;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoSystemException;
import com.dracoon.sdk.crypto.error.InvalidFileKeyException;
import com.dracoon.sdk.crypto.error.InvalidKeyPairException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.util.concurrent.Uninterruptibles;

public class SDSDirectS3UploadFeature extends HttpUploadFeature<Node, MessageDigest> {
    private static final Logger log = LogManager.getLogger(SDSDirectS3UploadFeature.class);

    /**
     * The maximum allowed parts in a multipart upload.
     */
    public static final int MAXIMUM_UPLOAD_PARTS = 10000;

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    private final Long partsize;
    private final Integer concurrency;

    public SDSDirectS3UploadFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final Write<Node> writer) {
        this(session, nodeid, writer, new HostPreferences(session.getHost()).getLong("s3.upload.multipart.size"),
                new HostPreferences(session.getHost()).getInteger("s3.upload.multipart.concurrency"));
    }

    public SDSDirectS3UploadFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final Write<Node> writer, final Long partsize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.nodeid = nodeid;
        this.partsize = partsize;
        this.concurrency = concurrency;
    }

    @Override
    public Node upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                       final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("multipart", concurrency);
        try {
            final CreateFileUploadRequest createFileUploadRequest = new CreateFileUploadRequest()
                    .directS3Upload(true)
                    .timestampModification(status.getTimestamp() != null ? new DateTime(status.getTimestamp()) : null)
                    .size(TransferStatus.UNKNOWN_LENGTH == status.getLength() ? null : status.getLength())
                    .parentId(Long.parseLong(nodeid.getVersionId(file.getParent(), new DisabledListProgressListener())))
                    .name(file.getName());
            final CreateFileUploadResponse createFileUploadResponse = new NodesApi(session.getClient())
                    .createFileUploadChannel(createFileUploadRequest, StringUtils.EMPTY);
            if(log.isDebugEnabled()) {
                log.debug(String.format("upload started for %s with response %s", file, createFileUploadResponse));
            }
            final Map<Integer, TransferStatus> etags = new HashMap<>();
            final List<PresignedUrl> presignedUrls = this.retrievePresignedUrls(createFileUploadResponse, status);
            final List<Future<TransferStatus>> parts = new ArrayList<>();
            // Full size of file
            final long size = status.getLength() + status.getOffset();
            long offset = 0;
            long remaining = status.getLength();
            for(int partNumber = 1; remaining >= 0; partNumber++) {
                final long length = Math.min(Math.max((size / (MAXIMUM_UPLOAD_PARTS - 1)), partsize), remaining);
                final PresignedUrl presignedUrl = presignedUrls.get(partNumber - 1);
                parts.add(this.submit(pool, file, local, throttle, listener, status,
                        presignedUrl.getUrl(), presignedUrl.getPartNumber(), offset, length, callback));
                remaining -= length;
                offset += length;
                if(0L == remaining) {
                    break;
                }
            }
            for(Future<TransferStatus> future : parts) {
                try {
                    final TransferStatus part = future.get();
                    etags.put(part.getPart(), part);
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
            final CompleteS3FileUploadRequest completeS3FileUploadRequest = new CompleteS3FileUploadRequest()
                    .keepShareLinks(status.isExists() ? new HostPreferences(session.getHost()).getBoolean("sds.upload.sharelinks.keep") : false)
                    .resolutionStrategy(status.isExists() ? CompleteS3FileUploadRequest.ResolutionStrategyEnum.OVERWRITE : CompleteS3FileUploadRequest.ResolutionStrategyEnum.FAIL);
            if(status.getFilekey() != null) {
                final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                final FileKey fileKey = reader.readValue(status.getFilekey().array());
                // Tag must not be null or empty with this version
                fileKey.setTag(null);
                final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(
                        TripleCryptConverter.toCryptoPlainFileKey(fileKey),
                        TripleCryptConverter.toCryptoUserPublicKey(session.keyPair().getPublicKeyContainer())
                );
                completeS3FileUploadRequest.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
            }
            etags.forEach((key, value) -> completeS3FileUploadRequest.addPartsItem(
                    new S3FileUploadPart().partEtag(value.getChecksum().hash).partNumber(key)));
            if(log.isDebugEnabled()) {
                log.debug(String.format("Complete file upload with %s for %s", completeS3FileUploadRequest, file));
            }
            new NodesApi(session.getClient()).completeS3FileUpload(completeS3FileUploadRequest, createFileUploadResponse.getUploadId(), StringUtils.EMPTY);
            // Polling
            final ScheduledThreadPool polling = new ScheduledThreadPool();
            final CountDownLatch done = new CountDownLatch(1);
            final AtomicReference<BackgroundException> failure = new AtomicReference<>();
            final AtomicReference<S3FileUploadStatus> uploadStatus = new AtomicReference<>();
            final ScheduledFuture f = polling.repeat(new Runnable() {
                @Override
                public void run() {
                    try {
                        uploadStatus.set(new NodesApi(session.getClient())
                                .requestUploadStatusFiles(createFileUploadResponse.getUploadId(), StringUtils.EMPTY, null));
                        switch(uploadStatus.get().getStatus()) {
                            case "finishing":
                                // Expected
                                break;
                            case "transfer":
                                failure.set(new InteroperabilityException(uploadStatus.get().getStatus()));
                                done.countDown();
                                break;
                            case "error":
                                failure.set(new InteroperabilityException(uploadStatus.get().getErrorDetails().getMessage()));
                                done.countDown();
                                break;
                            case "done":
                                // Set node id in transfer status
                                nodeid.cache(file, String.valueOf(uploadStatus.get().getNode().getId()));
                                done.countDown();
                                break;
                        }
                    }
                    catch(ApiException e) {
                        failure.set(new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file));
                        done.countDown();
                    }
                }
            }, new HostPreferences(session.getHost()).getLong("sds.upload.s3.status.period"), TimeUnit.MILLISECONDS);
            Uninterruptibles.awaitUninterruptibly(done);
            polling.shutdown();
            if(null != failure.get()) {
                throw failure.get();
            }
            // Mark parent status as complete
            status.setComplete();
            return uploadStatus.get().getNode();
        }
        catch(CryptoSystemException | InvalidFileKeyException | InvalidKeyPairException | UnknownVersionException e) {
            throw new TripleCryptExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            // Cancel future tasks
            pool.shutdown(false);
        }
    }

    private List<PresignedUrl> retrievePresignedUrls(final CreateFileUploadResponse createFileUploadResponse,
                                                     final TransferStatus status) throws ApiException {
        // Full size of file
        final long size = status.getLength() + status.getOffset();
        final List<PresignedUrl> presignedUrls = new ArrayList<>();
        final GeneratePresignedUrlsRequest presignedUrlsRequest = new GeneratePresignedUrlsRequest().firstPartNumber(1);
        {
            long remaining = status.getLength();
            // Determine number of parts
            for(int partNumber = 1; remaining >= 0; partNumber++) {
                final long length = Math.min(Math.max((size / (MAXIMUM_UPLOAD_PARTS - 1)), partsize), remaining);
                if(partNumber > 1 && length < Math.max((size / (MAXIMUM_UPLOAD_PARTS - 1)), partsize)) {
                    // Separate last part with non default part size
                    presignedUrls.addAll(new NodesApi(session.getClient()).generatePresignedUrlsFiles(
                            new GeneratePresignedUrlsRequest().firstPartNumber(partNumber).lastPartNumber(partNumber).size(length),
                            createFileUploadResponse.getUploadId(), StringUtils.EMPTY).getUrls());
                }
                else {
                    presignedUrlsRequest.lastPartNumber(partNumber).size(length);
                }
                remaining -= length;
                if(0L == remaining) {
                    break;
                }
            }
        }
        presignedUrls.addAll(0, new NodesApi(session.getClient()).generatePresignedUrlsFiles(presignedUrlsRequest,
                createFileUploadResponse.getUploadId(), StringUtils.EMPTY).getUrls());
        return presignedUrls;
    }

    private Future<TransferStatus> submit(final ThreadPool pool, final Path file, final Local local,
                                          final BandwidthThrottle throttle, final StreamListener listener,
                                          final TransferStatus overall, final String url, final Integer partNumber,
                                          final long offset, final long length, final ConnectionCallback callback) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit part %d of %s to queue with offset %d and length %d", partNumber, file, offset, length));
        }
        return pool.execute(new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<TransferStatus>() {
            @Override
            public TransferStatus call() throws BackgroundException {
                overall.validate();
                final TransferStatus status = new TransferStatus()
                        .segment(true)
                        .withLength(length)
                        .withOffset(offset);
                status.setUrl(url);
                status.setPart(partNumber);
                status.setHeader(overall.getHeader());
                status.setFilekey(overall.getFilekey());
                SDSDirectS3UploadFeature.super.upload(
                        file, local, throttle, listener, status, overall, status, callback);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Received response for part number %d", partNumber));
                }
                return status;
            }
        }, overall));
    }
}
