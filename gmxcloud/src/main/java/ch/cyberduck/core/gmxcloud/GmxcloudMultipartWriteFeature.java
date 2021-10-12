package ch.cyberduck.core.gmxcloud;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationRepresentationArrayInner;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
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

public class GmxcloudMultipartWriteFeature implements MultipartWrite<GmxcloudUploadResponse> {
    private static final Logger log = Logger.getLogger(GmxcloudMultipartWriteFeature.class);

    private final GmxcloudSession session;
    private final GmxcloudIdProvider fileid;

    public GmxcloudMultipartWriteFeature(final GmxcloudSession session, final GmxcloudIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public HttpResponseOutputStream<GmxcloudUploadResponse> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final String resourceId = fileid.getFileId(file.getParent(), new DisabledListProgressListener());
        final ResourceCreationResponseEntry uploadResourceCreationResponseEntry =
            GmxcloudUploadHelper.getUploadResourceCreationResponseEntry(session, file, ResourceCreationRepresentationArrayInner.UploadTypeEnum.CHUNKED, resourceId);
        final MultipartOutputStream proxy = new MultipartOutputStream(uploadResourceCreationResponseEntry.getEntity().getUploadURI(), file, status,
            Util.getResourceIdFromResourceUri(uploadResourceCreationResponseEntry.getHeaders().getLocation()));
        return new HttpResponseOutputStream<GmxcloudUploadResponse>(new MemorySegementingOutputStream(proxy,
            new HostPreferences(session.getHost()).getInteger("gmxcloud.upload.multipart.size"))) {
            @Override
            public GmxcloudUploadResponse getStatus() {
                return proxy.getResult();
            }
        };
    }

    private final class MultipartOutputStream extends OutputStream {
        private final String uploadUri;
        private final Path file;
        private final String resourceId;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();
        private final AtomicReference<BackgroundException> canceled = new AtomicReference<>();
        private final AtomicReference<GmxcloudUploadResponse> result = new AtomicReference<>();
        private Long offset = 0L;
        private Long cumulativeLength = 0L;
        private MessageDigest messageDigest;

        public MultipartOutputStream(final String uploadUri, final Path file, final TransferStatus status, final String resourceId) {
            this.uploadUri = uploadUri;
            this.file = file;
            this.overall = status;
            this.resourceId = resourceId;
        }

        @Override
        public void write(final int value) throws IOException {
            throw new IOException(new UnsupportedOperationException());
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            try {
                if(messageDigest == null) {
                    messageDigest = MessageDigest.getInstance("SHA-256");
                }
            }
            catch(NoSuchAlgorithmException e) {
                throw new IOException(e.getMessage(), e);
            }
            try {
                if(null != canceled.get()) {
                    throw canceled.get();
                }
                final byte[] content = Arrays.copyOfRange(b, off, len);
                new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<GmxcloudUploadResponse>() {

                    @Override
                    public GmxcloudUploadResponse call() throws BackgroundException {
                        final CloseableHttpClient client = session.getClient();
                        try {
                            final HttpEntity entity = EntityBuilder.create().setBinary(content).build();
                            final String hash = new SHA256ChecksumCompute()
                                .compute(new ByteArrayInputStream(content, off, len), new TransferStatus()).hash;
                            messageDigest.update(Hex.decodeHex(hash));
                            messageDigest.update(Util.intToBytes(content.length, 4));
                            final String offsetHashAndSizeIncludedUri = uploadUri + Constant.X_OFFSET + offset + Constant.X_SHA256 + hash + Constant.X_SIZE + content.length;
                            final HttpPut request = new HttpPut(offsetHashAndSizeIncludedUri);
                            request.setEntity(entity);
                            final HttpResponse response = client.execute(request);
                            try {
                                switch(response.getStatusLine().getStatusCode()) {
                                    case HttpStatus.SC_OK:
                                    case HttpStatus.SC_CREATED:
                                        final GmxcloudUploadResponse gmxcloudUploadResponse = GmxcloudUploadHelper.getGmxcloudUploadResponse(response);
                                        result.set(gmxcloudUploadResponse);
                                    case HttpStatus.SC_NO_CONTENT:
                                        offset += content.length;
                                        cumulativeLength += content.length;
                                        break;
                                    default:
                                        throw new GmxcloudExceptionMappingService().map(new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                            EntityUtils.toString(response.getEntity())));
                                }
                            }
                            catch(BackgroundException e) {
                                new GmxcloudWriteFeature(session, fileid).cancel(file, resourceId);
                                final BackgroundException backgroundException = new BackgroundException(e);
                                canceled.set(backgroundException);
                                throw backgroundException;
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
                if(overall.getLength() <= 0) {
                    final GmxcloudMultipartUploadCompleter gmxcloudMultipartUploadCompleter = new GmxcloudMultipartUploadCompleter(session);
                    final String cdash64 = Base64.encodeBase64URLSafeString(messageDigest.digest());
                    result.set(gmxcloudMultipartUploadCompleter.getCompletedUploadResponse(uploadUri, cumulativeLength, cdash64));
                    messageDigest.reset();
                }
            }
            finally {
                close.set(true);
            }
        }

        @Override
        public String toString() {
            return "MultipartOutputStream{" + "id='" + uploadUri + '\'' +
                ", file=" + file +
                '}';
        }

        public GmxcloudUploadResponse getResult() {
            return result.get();
        }
    }

}
