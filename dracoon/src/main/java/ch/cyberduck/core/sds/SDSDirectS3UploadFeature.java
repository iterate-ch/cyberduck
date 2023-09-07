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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Buffer;
import ch.cyberduck.core.io.BufferOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.FileBuffer;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.local.TemporaryFileService;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
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
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.SegmentRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoSystemException;
import com.dracoon.sdk.crypto.error.InvalidFileKeyException;
import com.dracoon.sdk.crypto.error.InvalidKeyPairException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.fasterxml.jackson.databind.ObjectReader;

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
    private final TemporaryFileService temp = TemporaryFileServiceFactory.instance();

    private final PathContainerService containerService
            = new SDSPathContainerService();

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
                    .timestampModification(status.getModified() != null ? new DateTime(status.getModified()) : null)
                    .size(TransferStatus.UNKNOWN_LENGTH == status.getLength() ? null : status.getLength())
                    .parentId(Long.parseLong(nodeid.getVersionId(file.getParent())))
                    .name(file.getName());
            final CreateFileUploadResponse createFileUploadResponse = new NodesApi(session.getClient())
                    .createFileUploadChannel(createFileUploadRequest, StringUtils.EMPTY);
            if(log.isDebugEnabled()) {
                log.debug(String.format("upload started for %s with response %s", file, createFileUploadResponse));
            }
            final Map<Integer, TransferStatus> etags = new HashMap<>();
            final List<PresignedUrl> presignedUrls = this.retrievePresignedUrls(createFileUploadResponse, status);
            final List<Future<TransferStatus>> parts = new ArrayList<>();
            final InputStream in;
            final String random = new UUIDRandomStringService().random();
            if(new SDSTripleCryptEncryptorFeature(session, nodeid).isEncrypted(containerService.getContainer(file))) {
                in = new SDSTripleCryptEncryptorFeature(session, nodeid).encrypt(file, local.getInputStream(), status);
            }
            else {
                in = local.getInputStream();
            }
            try {
                // Full size of file
                final long size = status.getLength() + status.getOffset();
                long offset = 0;
                long remaining = status.getLength();
                for(int partNumber = 1; remaining >= 0; partNumber++) {
                    final long length = Math.min(Math.max((size / (MAXIMUM_UPLOAD_PARTS - 1)), partsize), remaining);
                    final PresignedUrl presignedUrl = presignedUrls.get(partNumber - 1);
                    if(new SDSTripleCryptEncryptorFeature(session, nodeid).isEncrypted(containerService.getContainer(file))) {
                        final Local temporary = temp.create(String.format("%s-%d", random, partNumber));
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Encrypted contents for part %d to %s", partNumber, temporary));
                        }
                        final FileBuffer buffer = new FileBuffer(temporary);
                        new StreamCopier(status, StreamProgress.noop).withAutoclose(false).withLimit(length)
                                .transfer(in, new BufferOutputStream(buffer));
                        parts.add(this.submit(pool, file, temporary, buffer, throttle, listener, status,
                                presignedUrl.getUrl(), presignedUrl.getPartNumber(), 0L, length, callback));
                    }
                    else {
                        parts.add(this.submit(pool, file, local, Buffer.noop, throttle, listener, status,
                                presignedUrl.getUrl(), presignedUrl.getPartNumber(), offset, length, callback));
                    }
                    remaining -= length;
                    offset += length;
                    if(0L == remaining) {
                        break;
                    }
                }
            }
            finally {
                in.close();
            }
            Interruptibles.awaitAll(parts)
                    .forEach(part -> etags.put(part.getPart(), part));
            final CompleteS3FileUploadRequest completeS3FileUploadRequest = new CompleteS3FileUploadRequest()
                    .keepShareLinks(new HostPreferences(session.getHost()).getBoolean("sds.upload.sharelinks.keep"))
                    .resolutionStrategy(CompleteS3FileUploadRequest.ResolutionStrategyEnum.OVERWRITE);
            if(status.getFilekey() != null) {
                final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                final FileKey fileKey = reader.readValue(status.getFilekey().array());
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
            return new SDSUploadService(session, nodeid).await(file, status, createFileUploadResponse.getUploadId()).getNode();
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
            temp.shutdown();
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
                                          final Buffer buffer, final BandwidthThrottle throttle, final StreamListener listener,
                                          final TransferStatus overall, final String url, final Integer partNumber,
                                          final long offset, final long length, final ConnectionCallback callback) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit part %d of %s to queue with offset %d and length %d", partNumber, file, offset, length));
        }
        final BytecountStreamListener counter = new BytecountStreamListener(listener);
        return pool.execute(new SegmentRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<TransferStatus>() {
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
                final Node node = SDSDirectS3UploadFeature.super.upload(
                        file, local, throttle, counter, status, overall, status, callback);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Received response for part number %d", partNumber));
                }
                // Delete temporary file if any
                buffer.close();
                return status.withChecksum(Checksum.parse(node.getHash()));
            }
        }, overall, counter));
    }
}
