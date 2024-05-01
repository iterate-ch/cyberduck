package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.UploadType;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class EueMultipartWriteFeature implements MultipartWrite<EueWriteFeature.Chunk> {
    private static final Logger log = LogManager.getLogger(EueMultipartWriteFeature.class);

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueMultipartWriteFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public HttpResponseOutputStream<EueWriteFeature.Chunk> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        String uploadUri;
        String resourceId;
        if(status.isExists()) {
            resourceId = fileid.getFileId(file);
            uploadUri = EueUploadHelper.updateResource(session,
                    resourceId, status, UploadType.CHUNKED).getUploadURI();
        }
        else {
            final ResourceCreationResponseEntry resourceCreationResponseEntry =
                    EueUploadHelper.createResource(session, fileid.getFileId(file.getParent()), file.getName(),
                            status, UploadType.CHUNKED);
            resourceId = EueResourceIdProvider.getResourceIdFromResourceUri(resourceCreationResponseEntry.getHeaders().getLocation());
            uploadUri = resourceCreationResponseEntry.getEntity().getUploadURI();
        }
        final MultipartOutputStream proxy;
        try {
            proxy = new MultipartOutputStream(file, resourceId, uploadUri, status, callback);
        }
        catch(NoSuchAlgorithmException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e);
        }
        return new HttpResponseOutputStream<EueWriteFeature.Chunk>(new MemorySegementingOutputStream(proxy,
                new HostPreferences(session.getHost()).getInteger("eue.upload.multipart.size")),
                new EueAttributesAdapter(), status) {
            @Override
            public EueWriteFeature.Chunk getStatus() {
                return proxy.getResult();
            }
        };
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new SHA256ChecksumCompute();
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
    }

    private final class MultipartOutputStream extends OutputStream {
        private final Path file;
        private final String resourceId;
        private final String uploadUri;
        private final TransferStatus overall;
        private final ConnectionCallback callback;
        private final AtomicBoolean close = new AtomicBoolean();
        private final AtomicReference<BackgroundException> canceled = new AtomicReference<>();
        private final AtomicReference<EueWriteFeature.Chunk> result = new AtomicReference<>();
        private final MessageDigest messageDigest;

        private Long offset = 0L;

        public MultipartOutputStream(final Path file, final String resourceId, final String uploadUri,
                                     final TransferStatus status, final ConnectionCallback callback) throws NoSuchAlgorithmException {
            this.file = file;
            this.resourceId = resourceId;
            this.uploadUri = uploadUri;
            this.overall = status;
            this.callback = callback;
            this.messageDigest = MessageDigest.getInstance("SHA-256");
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
                if(0L == offset && content.length < new HostPreferences(session.getHost()).getLong("eue.upload.multipart.threshold")) {
                    final EueWriteFeature writer = new EueWriteFeature(session, fileid);
                    log.warn(String.format("Cancel chunked upload for %s", file));
                    writer.cancel(uploadUri);
                    final TransferStatus status = new TransferStatus(overall).withLength(content.length);
                    final HttpResponseOutputStream<EueWriteFeature.Chunk> stream = writer.write(file,
                            status.withChecksum(writer.checksum(file, overall).compute(new ByteArrayInputStream(content), status)), callback);
                    stream.write(content);
                    stream.close();
                    result.set(stream.getStatus());
                }
                else {
                    new DefaultRetryCallable<EueUploadHelper.UploadResponse>(session.getHost(), new BackgroundExceptionCallable<EueUploadHelper.UploadResponse>() {
                        @Override
                        public EueUploadHelper.UploadResponse call() throws BackgroundException {
                            final CloseableHttpClient client = session.getClient();
                            try {
                                final String hash = new SHA256ChecksumCompute()
                                        .compute(new ByteArrayInputStream(content), new TransferStatus()).hash;
                                messageDigest.update(Hex.decodeHex(hash));
                                messageDigest.update(ChunkListSHA256ChecksumCompute.intToBytes(content.length));
                                final HttpPut request = new HttpPut(String.format("%s&x_offset=%d&x_sha256=%s&x_size=%d",
                                        uploadUri, offset, hash, content.length));
                                request.setEntity(EntityBuilder.create().setBinary(content).build());
                                final HttpResponse response = client.execute(request);
                                try {
                                    switch(response.getStatusLine().getStatusCode()) {
                                        case HttpStatus.SC_OK:
                                        case HttpStatus.SC_CREATED:
                                        case HttpStatus.SC_NO_CONTENT:
                                            break;
                                        default:
                                            final ApiException failure = new ApiException(response.getStatusLine().getStatusCode(),
                                                    response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                                    EntityUtils.toString(response.getEntity()));
                                            throw new EueExceptionMappingService().map("Upload {0} failed", failure, file);
                                    }
                                }
                                catch(BackgroundException e) {
                                    canceled.set(e);
                                    throw e;
                                }
                                finally {
                                    EntityUtils.consume(response.getEntity());
                                }
                            }
                            catch(IOException | DecoderException e) {
                                throw new BackgroundException(e);
                            }
                            return null;
                        }
                    }, overall) {
                        @Override
                        public boolean retry(final BackgroundException failure, final ProgressListener progress, final BackgroundActionState cancel) {
                            if(super.retry(failure, progress, cancel)) {
                                canceled.set(null);
                                return true;
                            }
                            return false;
                        }
                    }.call();
                    offset += len;
                }
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
                    log.warn(String.format("Skip closing with previous failure %s", canceled.get()));
                    return;
                }
                // Skip complete if previously switched to simple upload because of smaller chunk size
                if(result.get() == null) {
                    if(0L == offset) {
                        this.write(new byte[0]);
                    }
                    else {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Complete chunked upload for %s", file));
                        }
                        try {
                            final String cdash64 = Base64.encodeBase64URLSafeString(messageDigest.digest());
                            final EueUploadHelper.UploadResponse completedUploadResponse = new EueMultipartUploadCompleter(session)
                                    .getCompletedUploadResponse(uploadUri, offset, cdash64);
                            if(!StringUtils.equals(cdash64, completedUploadResponse.getCdash64())) {
                                if(file.getType().contains(Path.Type.encrypted)) {
                                    log.warn(String.format("Skip checksum verification for %s with client side encryption enabled", file));
                                }
                                else {
                                    throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                                            MessageFormat.format("Mismatch between {0} hash {1} of uploaded data and ETag {2} returned by the server",
                                                    HashAlgorithm.cdash64, cdash64, completedUploadResponse.getCdash64()));
                                }
                            }
                            result.set(new EueWriteFeature.Chunk(resourceId, offset, cdash64));
                        }
                        catch(BackgroundException e) {
                            throw new IOException(e);
                        }
                    }
                }
            }
            finally {
                close.set(true);
            }
        }

        public EueWriteFeature.Chunk getResult() {
            return result.get();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MultipartOutputStream{");
            sb.append("file=").append(file);
            sb.append(", uploadUri='").append(uploadUri).append('\'');
            sb.append(", result=").append(result);
            sb.append(", offset=").append(offset);
            sb.append('}');
            return sb.toString();
        }
    }
}
