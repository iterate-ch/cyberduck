package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteS3FileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.GeneratePresignedUrlsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.PresignedUrl;
import ch.cyberduck.core.sds.io.swagger.client.model.S3FileUploadPart;
import ch.cyberduck.core.sds.io.swagger.client.model.S3FileUploadStatus;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoSystemException;
import com.dracoon.sdk.crypto.error.InvalidFileKeyException;
import com.dracoon.sdk.crypto.error.InvalidKeyPairException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.util.concurrent.Uninterruptibles;

public class PresignedMultipartOutputStream extends OutputStream {
    private static final Logger log = Logger.getLogger(PresignedMultipartOutputStream.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final CreateFileUploadResponse createFileUploadResponse;
    private final Path file;
    private final TransferStatus overall;
    private final AtomicBoolean close = new AtomicBoolean();
    private final AtomicReference<BackgroundException> canceled = new AtomicReference<>();

    private Long offset = 0L;
    private int partNumber;
    private final Map<Integer, String> etags = new HashMap<>();

    public PresignedMultipartOutputStream(final SDSSession session, final SDSNodeIdProvider nodeid,
                                          final CreateFileUploadResponse createFileUploadResponse,
                                          final Path file, final TransferStatus status) {
        this.session = session;
        this.nodeid = nodeid;
        this.createFileUploadResponse = createFileUploadResponse;
        this.file = file;
        this.overall = status;
    }

    @Override
    public void write(final int value) throws IOException {
        throw new IOException(new UnsupportedOperationException());
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        try {
            if(null != canceled.get()) {
                throw canceled.get();
            }
            final byte[] content = Arrays.copyOfRange(b, off, len);
            final HttpEntity entity = EntityBuilder.create().setBinary(content).build();
            new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<Void>() {
                @Override
                public Void call() throws BackgroundException {
                    final SDSApiClient client = session.getClient();
                    try {
                        final List<PresignedUrl> target = new NodesApi(session.getClient()).generatePresignedUrlsFiles(new GeneratePresignedUrlsRequest()
                                .firstPartNumber(++partNumber).lastPartNumber(partNumber).size((long) content.length),
                            createFileUploadResponse.getUploadId(), StringUtils.EMPTY).getUrls();
                        for(PresignedUrl url : target) {
                            final HttpPut request = new HttpPut(url.getUrl());
                            request.setEntity(entity);
                            request.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(entity.getContentLength()));
                            request.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeService.DEFAULT_CONTENT_TYPE);
                            final HttpResponse response = client.getClient().execute(request);
                            try {
                                // Validate response
                                switch(response.getStatusLine().getStatusCode()) {
                                    case HttpStatus.SC_OK:
                                        // Upload complete
                                        offset += content.length;
                                        if(response.containsHeader("ETag")) {
                                            etags.put(partNumber, response.getFirstHeader("ETag").getValue());
                                        }
                                        else {
                                            log.error(String.format("Missing ETag in response %s", response));
                                        }
                                        break;
                                    default:
                                        EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                                        throw new SDSExceptionMappingService(nodeid).map(
                                            new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                                EntityUtils.toString(response.getEntity())));
                                }
                            }
                            catch(BackgroundException e) {
                                // Cancel upload on error reply
                                canceled.set(e);
                                throw e;
                            }
                            finally {
                                EntityUtils.consume(response.getEntity());
                            }
                        }
                    }
                    catch(ApiException e) {
                        throw new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file);
                    }
                    catch(HttpResponseException e) {
                        throw new DefaultHttpResponseExceptionMappingService().map(e);
                    }
                    catch(IOException e) {
                        throw new DefaultIOExceptionMappingService().map(e);
                    }
                    return null; //Void
                }
            }, overall).call();
        }
        catch(BackgroundException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if(close.get()) {
                log.warn(String.format("Skip double close of stream %s", this));
                return;
            }
            if(null != canceled.get()) {
                return;
            }
            if(etags.isEmpty()) {
                new SDSTouchFeature(session, nodeid).touch(file, new TransferStatus());
            }
            else {
                try {
                    final CompleteS3FileUploadRequest completeS3FileUploadRequest = new CompleteS3FileUploadRequest()
                        .keepShareLinks(overall.isExists() ? PreferencesFactory.get().getBoolean("sds.upload.sharelinks.keep") : false)
                        .resolutionStrategy(overall.isExists() ? CompleteS3FileUploadRequest.ResolutionStrategyEnum.OVERWRITE : CompleteS3FileUploadRequest.ResolutionStrategyEnum.FAIL);
                    if(overall.getFilekey() != null) {
                        final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                        final FileKey fileKey = reader.readValue(overall.getFilekey().array());
                        final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(
                            TripleCryptConverter.toCryptoPlainFileKey(fileKey),
                            TripleCryptConverter.toCryptoUserPublicKey(session.keyPair().getPublicKeyContainer())
                        );
                        completeS3FileUploadRequest.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
                    }
                    etags.forEach((key, value) -> completeS3FileUploadRequest.addPartsItem(
                        new S3FileUploadPart().partEtag(StringUtils.remove(value, '"')).partNumber(key)));
                    new NodesApi(session.getClient()).completeS3FileUpload(completeS3FileUploadRequest, createFileUploadResponse.getUploadId(), StringUtils.EMPTY);
                    // Polling
                    final ScheduledThreadPool polling = new ScheduledThreadPool();
                    final CountDownLatch done = new CountDownLatch(1);
                    final AtomicReference<BackgroundException> failure = new AtomicReference<>();
                    final ScheduledFuture f = polling.repeat(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final S3FileUploadStatus uploadStatus = new NodesApi(session.getClient())
                                    .requestUploadStatusFiles(createFileUploadResponse.getUploadId(), StringUtils.EMPTY, null);
                                switch(uploadStatus.getStatus()) {
                                    case "finishing":
                                        // Expected
                                        break;
                                    case "transfer":
                                        failure.set(new InteroperabilityException(uploadStatus.getStatus()));
                                        done.countDown();
                                    case "error":
                                        failure.set(new InteroperabilityException(uploadStatus.getErrorDetails().getMessage()));
                                        done.countDown();
                                    case "done":
                                        nodeid.cache(file, String.valueOf(uploadStatus.getNode().getId()));
                                        done.countDown();
                                        break;
                                }
                            }
                            catch(ApiException e) {
                                done.countDown();
                                failure.set(new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file));
                            }
                        }
                    }, PreferencesFactory.get().getLong("sds.upload.s3.status.period"), TimeUnit.MILLISECONDS);
                    Uninterruptibles.awaitUninterruptibly(done);
                    polling.shutdown();
                    if(null != failure.get()) {
                        throw failure.get();
                    }
                }
                catch(CryptoSystemException | InvalidFileKeyException | InvalidKeyPairException | UnknownVersionException e) {
                    throw new TripleCryptExceptionMappingService().map("Upload {0} failed", e, file);
                }
                catch(ApiException e) {
                    throw new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file);
                }
            }
        }
        catch(BackgroundException e) {
            throw new IOException(e);
        }
        finally {
            close.set(true);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PresignedMultipartOutputStream{");
        sb.append("nodeid=").append(nodeid);
        sb.append(", createFileUploadResponse=").append(createFileUploadResponse);
        sb.append(", file=").append(file);
        sb.append(", overall=").append(overall);
        sb.append(", offset=").append(offset);
        sb.append('}');
        return sb.toString();
    }
}
