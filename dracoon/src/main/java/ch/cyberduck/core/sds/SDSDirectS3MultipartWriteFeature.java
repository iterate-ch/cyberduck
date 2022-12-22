package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
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
import ch.cyberduck.core.sds.io.swagger.client.model.PresignedUrlList;
import ch.cyberduck.core.sds.io.swagger.client.model.S3FileUploadPart;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.error.CryptoSystemException;
import com.dracoon.sdk.crypto.error.InvalidFileKeyException;
import com.dracoon.sdk.crypto.error.InvalidKeyPairException;
import com.dracoon.sdk.crypto.error.UnknownVersionException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.fasterxml.jackson.databind.ObjectReader;

public class SDSDirectS3MultipartWriteFeature extends AbstractHttpWriteFeature<Node> implements MultipartWrite<Node> {
    private static final Logger log = LogManager.getLogger(SDSDirectS3MultipartWriteFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final Integer partsize;

    public SDSDirectS3MultipartWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this(session, nodeid, new HostPreferences(session.getHost()).getInteger("sds.upload.multipart.chunksize"));
    }

    public SDSDirectS3MultipartWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final Integer partsize) {
        super(new SDSAttributesAdapter(session));
        this.session = session;
        this.nodeid = nodeid;
        this.partsize = partsize;
    }

    @Override
    public HttpResponseOutputStream<Node> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final CreateFileUploadRequest createFileUploadRequest = new CreateFileUploadRequest()
                    .directS3Upload(true)
                    .timestampModification(status.getTimestamp() != null ? new DateTime(status.getTimestamp()) : null)
                    .parentId(Long.parseLong(nodeid.getVersionId(file.getParent())))
                    .name(file.getName());
            final CreateFileUploadResponse createFileUploadResponse = new NodesApi(session.getClient())
                    .createFileUploadChannel(createFileUploadRequest, StringUtils.EMPTY);
            if(log.isDebugEnabled()) {
                log.debug(String.format("upload started for %s with response %s", file, createFileUploadResponse));
            }
            final MultipartOutputStream proxy = new MultipartOutputStream(createFileUploadResponse, file, status);
            return new HttpResponseOutputStream<Node>(new MemorySegementingOutputStream(proxy, partsize),
                    new SDSAttributesAdapter(session), status) {
                @Override
                public Node getStatus() {
                    return proxy.getResult();
                }
            };
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file);
        }

    }

    private final class MultipartOutputStream extends OutputStream {
        /**
         * Completed ETag of parts
         */
        private final Map<Integer, Checksum> completed = new HashMap<>();
        private final CreateFileUploadResponse createFileUploadResponse;
        private final Path file;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();
        private final AtomicReference<BackgroundException> canceled = new AtomicReference<>();
        private final AtomicReference<Node> result = new AtomicReference<>();

        private int partNumber;

        public MultipartOutputStream(final CreateFileUploadResponse createFileUploadResponse, final Path file, final TransferStatus status) {
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
                partNumber++;
                completed.put(partNumber, new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<Checksum>() {
                    @Override
                    public Checksum call() throws BackgroundException {
                        try {
                            final PresignedUrlList presignedUrlList = new NodesApi(session.getClient()).generatePresignedUrlsFiles(
                                new GeneratePresignedUrlsRequest().firstPartNumber(partNumber).lastPartNumber(partNumber).size((long) len),
                                createFileUploadResponse.getUploadId(), StringUtils.EMPTY);
                            for(PresignedUrl url : presignedUrlList.getUrls()) {
                                final HttpPut request = new HttpPut(url.getUrl());
                                request.setEntity(new ByteArrayEntity(b, off, len));
                                request.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeService.DEFAULT_CONTENT_TYPE);
                                final HttpResponse response = session.getClient().getClient().execute(request);
                                // Validate response
                                try {
                                    switch(response.getStatusLine().getStatusCode()) {
                                        case HttpStatus.SC_OK:
                                            // Upload complete
                                            if(response.containsHeader("ETag")) {
                                                if(log.isInfoEnabled()) {
                                                    log.info(String.format("Received response %s for part number %d", response, partNumber));
                                                }
                                                return Checksum.parse(StringUtils.remove(response.getFirstHeader("ETag").getValue(), '"'));
                                            }
                                            else {
                                                log.error(String.format("Missing ETag in response %s", response));
                                                throw new InteroperabilityException(response.getStatusLine().getReasonPhrase());
                                            }
                                        default:
                                            EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                                            throw new DefaultHttpResponseExceptionMappingService().map(
                                                new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
                                    }
                                }
                                finally {
                                    EntityUtils.consume(response.getEntity());
                                }
                            }
                            //
                            throw new InteroperabilityException().withFile(file);
                        }
                        catch(HttpResponseException e) {
                            throw new DefaultHttpResponseExceptionMappingService().map("Upload {0} failed", e, file);
                        }
                        catch(IOException e) {
                            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                        }
                        catch(ApiException e) {
                            throw new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file);
                        }
                    }
                }, overall).call());
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
                if(completed.isEmpty()) {
                    this.write(new byte[0]);
                }
                final CompleteS3FileUploadRequest completeS3FileUploadRequest = new CompleteS3FileUploadRequest()
                        .keepShareLinks(new HostPreferences(session.getHost()).getBoolean("sds.upload.sharelinks.keep"))
                        .resolutionStrategy(CompleteS3FileUploadRequest.ResolutionStrategyEnum.OVERWRITE);
                if(overall.getFilekey() != null) {
                    final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                    final FileKey fileKey = reader.readValue(overall.getFilekey().array());
                    final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(
                            TripleCryptConverter.toCryptoPlainFileKey(fileKey),
                            TripleCryptConverter.toCryptoUserPublicKey(session.keyPair().getPublicKeyContainer())
                    );
                    completeS3FileUploadRequest.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
                }
                completed.forEach((key, value) -> completeS3FileUploadRequest.addPartsItem(
                        new S3FileUploadPart().partEtag(value.hash).partNumber(key)));
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Complete file upload with %s for %s", completeS3FileUploadRequest, file));
                }
                new NodesApi(session.getClient()).completeS3FileUpload(completeS3FileUploadRequest, createFileUploadResponse.getUploadId(), StringUtils.EMPTY);
                // Polling
                result.set(new SDSUploadService(session, nodeid).await(file, overall, createFileUploadResponse.getUploadId()).getNode());
            }
            catch(BackgroundException e) {
                throw new IOException(e);
            }
            catch(ApiException e) {
                throw new IOException(new SDSExceptionMappingService(nodeid).map("Upload {0} failed", e, file));
            }
            catch(CryptoSystemException | InvalidFileKeyException | InvalidKeyPairException | UnknownVersionException e) {
                throw new IOException(new TripleCryptExceptionMappingService().map("Upload {0} failed", e, file));
            }
            finally {
                close.set(true);
            }
        }

        public Node getResult() {
            return result.get();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MultipartOutputStream{");
            sb.append("multipart=").append(createFileUploadResponse);
            sb.append(", file=").append(file);
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public boolean timestamp() {
        return true;
    }
}
