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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.UploadType;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class EueMultipartWriteFeature implements MultipartWrite<EueWriteFeature.Chunk> {
    private static final Logger log = Logger.getLogger(EueMultipartWriteFeature.class);

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueMultipartWriteFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public HttpResponseOutputStream<EueWriteFeature.Chunk> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        String uploadUri;
        if(status.isExists()) {
            uploadUri = EueUploadHelper.updateResource(session,
                    fileid.getFileId(file, new DisabledListProgressListener()), UploadType.CHUNKED).getUploadURI();
        }
        else {
            final ResourceCreationResponseEntry resourceCreationResponseEntry =
                    EueUploadHelper.createResource(session, fileid.getFileId(file.getParent(), new DisabledListProgressListener()), file.getName(),
                            status, UploadType.CHUNKED);
            uploadUri = resourceCreationResponseEntry.getEntity().getUploadURI();
        }
        final MultipartOutputStream proxy;
        try {
            proxy = new MultipartOutputStream(file, uploadUri, status, callback);
        }
        catch(NoSuchAlgorithmException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e);
        }
        return new HttpResponseOutputStream<EueWriteFeature.Chunk>(new MemorySegementingOutputStream(proxy,
                new HostPreferences(session.getHost()).getInteger("eue.upload.multipart.size"))) {
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

    private final class MultipartOutputStream extends OutputStream {
        private final Path file;
        private final String uploadUri;
        private final TransferStatus overall;
        private final ConnectionCallback callback;
        private final AtomicBoolean close = new AtomicBoolean();
        private final AtomicReference<BackgroundException> canceled = new AtomicReference<>();
        private final AtomicReference<EueWriteFeature.Chunk> result = new AtomicReference<>();
        private final MessageDigest messageDigest;

        private Long offset = 0L;

        public MultipartOutputStream(final Path file, final String uploadUri, final TransferStatus status, final ConnectionCallback callback) throws NoSuchAlgorithmException {
            this.file = file;
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
                    writer.cancel(uploadUri);
                    final HttpResponseOutputStream<EueWriteFeature.Chunk> stream = writer.write(file,
                            overall.withChecksum(writer.checksum(file, overall).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length))), callback);
                    stream.write(content);
                    stream.close();
                    result.set(stream.getStatus());
                }
                else {
                    new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<EueUploadHelper.UploadResponse>() {
                        @Override
                        public EueUploadHelper.UploadResponse call() throws BackgroundException {
                            final CloseableHttpClient client = session.getClient();
                            try {
                                final String hash = new SHA256ChecksumCompute()
                                        .compute(new ByteArrayInputStream(content, off, len), new TransferStatus()).hash;
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
                                            offset += content.length;
                                            break;
                                        default:
                                            throw new EueExceptionMappingService().map(new ApiException(response.getStatusLine().getStatusCode(),
                                                    response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                                    EntityUtils.toString(response.getEntity())));
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
                    }, overall).call();
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
                    return;
                }
                if(result.get() == null) {
                    try {
                        final String cdash64 = Base64.encodeBase64URLSafeString(messageDigest.digest());
                        new EueMultipartUploadCompleter(session)
                                .getCompletedUploadResponse(uploadUri, offset, cdash64);
                        result.set(new EueWriteFeature.Chunk(overall.getLength(), cdash64));
                    }
                    catch(BackgroundException e) {
                        throw new IOException(e);
                    }
                }
            }
            finally {
                close.set(true);
            }
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

        public EueWriteFeature.Chunk getResult() {
            return result.get();
        }
    }
}
