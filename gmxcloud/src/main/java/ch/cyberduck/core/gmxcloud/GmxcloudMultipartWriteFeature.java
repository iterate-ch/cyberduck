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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UploadType;
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

public class GmxcloudMultipartWriteFeature implements MultipartWrite<GmxcloudUploadHelper.GmxcloudUploadResponse> {
    private static final Logger log = Logger.getLogger(GmxcloudMultipartWriteFeature.class);

    private final GmxcloudSession session;
    private final GmxcloudResourceIdProvider fileid;

    public GmxcloudMultipartWriteFeature(final GmxcloudSession session, final GmxcloudResourceIdProvider fileid) {
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
    public HttpResponseOutputStream<GmxcloudUploadHelper.GmxcloudUploadResponse> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        String uploadUri;
        String resourceId;
        if(status.isExists()) {
            resourceId = fileid.getFileId(file, new DisabledListProgressListener());
            uploadUri = GmxcloudUploadHelper.updateResource(session, resourceId, UploadType.CHUNKED).getUploadURI();
        }
        else {
            final ResourceCreationResponseEntry resourceCreationResponseEntry =
                    GmxcloudUploadHelper.createResource(session, fileid.getFileId(file.getParent(), new DisabledListProgressListener()), file.getName(),
                            UploadType.CHUNKED);
            uploadUri = resourceCreationResponseEntry.getEntity().getUploadURI();
            resourceId = GmxcloudResourceIdProvider.getResourceIdFromResourceUri(resourceCreationResponseEntry.getHeaders().getLocation());
        }
        final MultipartOutputStream proxy;
        try {
            proxy = new MultipartOutputStream(uploadUri, resourceId, status);
        }
        catch(NoSuchAlgorithmException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e);
        }
        return new HttpResponseOutputStream<GmxcloudUploadHelper.GmxcloudUploadResponse>(new MemorySegementingOutputStream(proxy,
                new HostPreferences(session.getHost()).getInteger("gmxcloud.upload.multipart.size"))) {
            @Override
            public GmxcloudUploadHelper.GmxcloudUploadResponse getStatus() {
                return proxy.getResult();
            }
        };
    }

    private final class MultipartOutputStream extends OutputStream {
        private final String uploadUri;
        private final String resourceId;
        private final TransferStatus overall;
        private final AtomicBoolean close = new AtomicBoolean();
        private final AtomicReference<BackgroundException> canceled = new AtomicReference<>();
        private final AtomicReference<GmxcloudUploadHelper.GmxcloudUploadResponse> result = new AtomicReference<>();
        private final MessageDigest messageDigest;

        private Long offset = 0L;
        private Long cumulativeLength = 0L;

        public MultipartOutputStream(final String uploadUri, final String resourceId, final TransferStatus status) throws NoSuchAlgorithmException {
            this.uploadUri = uploadUri;
            this.resourceId = resourceId;
            this.overall = status;
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
                new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<GmxcloudUploadHelper.GmxcloudUploadResponse>() {

                    @Override
                    public GmxcloudUploadHelper.GmxcloudUploadResponse call() throws BackgroundException {
                        final CloseableHttpClient client = session.getClient();
                        try {
                            final HttpEntity entity = EntityBuilder.create().setBinary(content).build();
                            final String hash = new SHA256ChecksumCompute()
                                    .compute(new ByteArrayInputStream(content, off, len), new TransferStatus()).hash;
                            messageDigest.update(Hex.decodeHex(hash));
                            messageDigest.update(GmxcloudCdash64Compute.intToBytes(content.length));
                            final HttpPut request = new HttpPut(String.format("%s&x_offset=%d&x_sha256=%s&x_size=%d",
                                    uploadUri, offset, hash, content.length));
                            request.setEntity(entity);
                            final HttpResponse response = client.execute(request);
                            try {
                                switch(response.getStatusLine().getStatusCode()) {
                                    case HttpStatus.SC_OK:
                                    case HttpStatus.SC_CREATED:
                                        result.set(GmxcloudUploadHelper.parseUploadResponse(response));
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
                try {
                    final String cdash64 = Base64.encodeBase64URLSafeString(messageDigest.digest());
                    result.set(new GmxcloudMultipartUploadCompleter(session)
                            .getCompletedUploadResponse(uploadUri, cumulativeLength, cdash64));
                }
                catch(BackgroundException e) {
                    throw new IOException(e);
                }
            }
            finally {
                close.set(true);
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MultipartOutputStream{");
            sb.append("uploadUri='").append(uploadUri).append('\'');
            sb.append(", resourceId='").append(resourceId).append('\'');
            sb.append(", offset=").append(offset);
            sb.append('}');
            return sb.toString();
        }

        public GmxcloudUploadHelper.GmxcloudUploadResponse getResult() {
            return result.get();
        }
    }
}
