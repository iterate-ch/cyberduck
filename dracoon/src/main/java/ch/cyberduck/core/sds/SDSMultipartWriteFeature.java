package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DelayedHttpMultipartEntity;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.triplecrypt.CryptoExceptionMappingService;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.CryptoSystemException;
import com.dracoon.sdk.crypto.InvalidFileKeyException;
import com.dracoon.sdk.crypto.InvalidKeyPairException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.fasterxml.jackson.databind.ObjectReader;

import static ch.cyberduck.core.sds.SDSWriteFeature.DEFAULT_CLASSIFICATION;

public class SDSMultipartWriteFeature implements MultipartWrite<VersionId> {
    private static final Logger log = Logger.getLogger(SDSMultipartWriteFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final Find finder;
    private final AttributesFinder attributes;

    public SDSMultipartWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this(session, nodeid, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public SDSMultipartWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final Find finder, final AttributesFinder attributes) {
        this.session = session;
        this.nodeid = nodeid;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public HttpResponseOutputStream<VersionId> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final CreateFileUploadRequest body = new CreateFileUploadRequest();
        body.setParentId(Long.parseLong(nodeid.getFileid(file.getParent(), new DisabledListProgressListener())));
        body.setName(file.getName());
        body.classification(DEFAULT_CLASSIFICATION); // internal
        try {
            final CreateFileUploadResponse response = new NodesApi(session.getClient()).createFileUpload(body, StringUtils.EMPTY);
            final String id = response.getUploadId();
            final MultipartOutputStream proxy = new MultipartOutputStream(id, file, status);
            return new HttpResponseOutputStream<VersionId>(new MemorySegementingOutputStream(proxy,
                PreferencesFactory.get().getInteger("sds.upload.multipart.chunksize"))) {
                @Override
                public VersionId getStatus() throws BackgroundException {
                    return proxy.getVersionId();
                }
            };
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map(e);
        }
    }

    private final class MultipartOutputStream extends OutputStream {
        private final String uploadId;
        private final Path file;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();

        private Long offset = 0L;

        private VersionId versionId;

        public MultipartOutputStream(final String uploadId, final Path file, final TransferStatus status) {
            this.uploadId = uploadId;
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
                final byte[] content = Arrays.copyOfRange(b, off, len);
                final HttpEntity entity = MultipartEntityBuilder.create()
                    .setBoundary(DelayedHttpMultipartEntity.DEFAULT_BOUNDARY)
                    .addPart("file", new ByteArrayBody(content, file.getName()))
                    .build();
                new DefaultRetryCallable<Void>(new BackgroundExceptionCallable<Void>() {
                    @Override
                    public Void call() throws BackgroundException {
                        final SDSApiClient client = session.getClient();
                        try {
                            final HttpPost request = new HttpPost(String.format("%s/v4/nodes/files/uploads/%s", client.getBasePath(), uploadId));
                            request.setEntity(entity);
                            request.setHeader(SDSSession.SDS_AUTH_TOKEN_HEADER, StringUtils.EMPTY);
                            request.setHeader(HTTP.CONTENT_TYPE, String.format("multipart/form-data; boundary=%s", DelayedHttpMultipartEntity.DEFAULT_BOUNDARY));
                            if(0L != overall.getLength() && 0 != content.length) {
                                final HttpRange range = HttpRange.byLength(offset, content.length);
                                final String header;
                                if(overall.getLength() == -1L) {
                                    header = String.format("%d-%d/*", range.getStart(), range.getEnd());
                                }
                                else {
                                    header = String.format("%d-%d/%d", range.getStart(), range.getEnd(), overall.getOffset() + overall.getLength());
                                }
                                request.addHeader(HttpHeaders.CONTENT_RANGE, String.format("bytes %s", header));
                            }
                            final HttpResponse response = client.getClient().execute(request);
                            try {
                                // Validate response
                                switch(response.getStatusLine().getStatusCode()) {
                                    case HttpStatus.SC_CREATED:
                                        // Upload complete
                                        offset += content.length;
                                        break;
                                    default:
                                        EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                                        throw new SDSExceptionMappingService().map(
                                            new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                                EntityUtils.toString(response.getEntity())));
                                }
                            }
                            finally {
                                EntityUtils.consume(response.getEntity());
                            }
                        }
                        catch(IOException e) {
                            try {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Cancel failed upload %s for %s", uploadId, file));
                                }
                                new NodesApi(session.getClient()).cancelFileUpload(uploadId, StringUtils.EMPTY);
                            }
                            catch(ApiException f) {
                                throw new SDSExceptionMappingService().map(f);
                            }
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
                final CompleteUploadRequest body = new CompleteUploadRequest()
                    .resolutionStrategy(overall.isExists() ? CompleteUploadRequest.ResolutionStrategyEnum.OVERWRITE : CompleteUploadRequest.ResolutionStrategyEnum.FAIL);
                if(overall.getFilekey() != null) {
                    final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                    final FileKey fileKey = reader.readValue(overall.getFilekey().array());
                    final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(
                        TripleCryptConverter.toCryptoPlainFileKey(fileKey),
                        TripleCryptConverter.toCryptoUserPublicKey(session.keyPair().getPublicKeyContainer())
                    );
                    body.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
                }
                final Node upload = new NodesApi(session.getClient()).completeFileUpload(uploadId, body, StringUtils.EMPTY, null);
                versionId = new VersionId(String.valueOf(upload.getId()));
            }
            catch(ApiException e) {
                throw new IOException(new SDSExceptionMappingService().map("Upload {0} failed", e, file));
            }
            catch(CryptoSystemException | InvalidFileKeyException | InvalidKeyPairException e) {
                throw new IOException(new CryptoExceptionMappingService().map("Upload {0} failed", e, file));
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
            final StringBuilder sb = new StringBuilder("MultipartOutputStream{");
            sb.append("id='").append(uploadId).append('\'');
            sb.append(", file=").append(file);
            sb.append('}');
            return sb.toString();
        }

        public VersionId getVersionId() {
            return versionId;
        }
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attr = attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attr.getSize()).withChecksum(attr.getChecksum());
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }
}
