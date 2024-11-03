package ch.cyberduck.core.brick;

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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.brick.io.swagger.client.model.FileEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.FileUploadPartEntity;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class BrickWriteFeature extends AbstractHttpWriteFeature<FileEntity> {
    private static final Logger log = LogManager.getLogger(BrickWriteFeature.class);

    private final BrickSession session;

    public BrickWriteFeature(final BrickSession session) {
        super(new BrickAttributesFinderFeature(session));
        this.session = session;
    }

    @Override
    public HttpResponseOutputStream<FileEntity> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final String uploadUri;
        FileUploadPartEntity uploadPartEntity = null;
        if(StringUtils.isBlank(status.getUrl())) {
            uploadPartEntity = new BrickUploadFeature(session, this).startUpload(file);
            uploadUri = uploadPartEntity.getUploadUri();
        }
        else {
            uploadUri = status.getUrl();
        }
        final HttpResponseOutputStream<FileEntity> stream = this.write(file, status, new DelayedHttpEntityCallable<FileEntity>(file) {
            @Override
            public FileEntity call(final HttpEntity entity) throws BackgroundException {
                try {
                    final HttpPut request = new HttpPut(uploadUri);
                    request.setEntity(entity);
                    request.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeService.DEFAULT_CONTENT_TYPE);
                    final HttpResponse response = session.getClient().execute(request);
                    // Validate response
                    try {
                        switch(response.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                                if(log.isInfoEnabled()) {
                                    log.info("Received response {} for part number {}", response, status.getPart());
                                }
                                // Upload complete
                                if(response.containsHeader("ETag")) {
                                    if(file.getType().contains(Path.Type.encrypted)) {
                                        log.warn("Skip checksum verification for {} with client side encryption enabled", file);
                                    }
                                    else {
                                        if(HashAlgorithm.md5.equals(status.getChecksum().algorithm)) {
                                            final Checksum etag = Checksum.parse(StringUtils.remove(response.getFirstHeader("ETag").getValue(), '"'));
                                            if(!status.getChecksum().equals(etag)) {
                                                throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                                                        MessageFormat.format("Mismatch between {0} hash {1} of uploaded data and ETag {2} returned by the server",
                                                                etag.algorithm.toString(), status.getChecksum().hash, etag.hash));
                                            }
                                        }
                                    }
                                }
                                else {
                                    if(log.isDebugEnabled()) {
                                        log.debug("No ETag header in response available");
                                    }
                                }
                                return null;
                            default:
                                EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                                throw new DefaultHttpResponseExceptionMappingService().map("Upload {0} failed",
                                        new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()), file);
                        }
                    }
                    finally {
                        EntityUtils.consume(response.getEntity());
                    }
                }
                catch(HttpResponseException e) {
                    throw new DefaultHttpResponseExceptionMappingService().map("Upload {0} failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        });
        if(StringUtils.isBlank(status.getUrl())) {
            final String ref = uploadPartEntity.getRef();
            return new HttpResponseOutputStream<FileEntity>(new ProxyOutputStream(stream),
                    new BrickAttributesFinderFeature(session), status) {
                private final AtomicBoolean close = new AtomicBoolean();

                @Override
                public FileEntity getStatus() throws BackgroundException {
                    return stream.getStatus();
                }

                @Override
                public void close() throws IOException {
                    if(close.get()) {
                        log.warn("Skip double close of stream {}", this);
                        return;
                    }
                    super.close();
                    try {
                        new BrickUploadFeature(session, BrickWriteFeature.this)
                                .completeUpload(file, ref, status, Collections.singletonList(status));
                    }
                    catch(BackgroundException e) {
                        throw new IOException(e.getMessage(), e);
                    }
                    finally {
                        close.set(true);
                    }
                }
            };
        }
        return stream;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new MD5ChecksumCompute();
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
    }
}
