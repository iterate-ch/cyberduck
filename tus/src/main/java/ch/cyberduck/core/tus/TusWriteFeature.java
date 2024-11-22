package ch.cyberduck.core.tus;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VoidAttributesAdapter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import static ch.cyberduck.core.tus.TusCapabilities.*;

public class TusWriteFeature extends AbstractHttpWriteFeature<Void> {
    private static final Logger log = LogManager.getLogger(TusWriteFeature.class);

    private final TusCapabilities capabilities;
    private final HttpClient client;

    public TusWriteFeature(final TusCapabilities capabilities, final HttpClient client) {
        super(new VoidAttributesAdapter());
        this.capabilities = capabilities;
        this.client = client;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DelayedHttpEntityCallable<Void> command = new DelayedHttpEntityCallable<Void>(file) {
            @Override
            public Void call(final HttpEntity entity) throws BackgroundException {
                final HttpPatch request = new HttpPatch(status.getParameters().get(TusUploadFeature.UPLOAD_URL));
                request.setEntity(entity);
                request.setHeader(TUS_HEADER_RESUMABLE, TUS_VERSION);
                final Checksum checksum = status.getChecksum();
                if(Checksum.NONE != checksum) {
                    request.setHeader(TUS_HEADER_UPLOAD_CHECKSUM, String.format("%s %s", checksum.algorithm, checksum.hex));
                }
                request.setHeader(TUS_HEADER_UPLOAD_OFFSET, String.valueOf(status.getOffset()));
                // All PATCH requests MUST use Content-Type: application/offset+octet-stream
                request.setHeader(HttpHeaders.CONTENT_TYPE, "application/offset+octet-stream");
                try {
                    return client.execute(request, new ResponseHandler<Void>() {
                        @Override
                        public Void handleResponse(final HttpResponse response) throws HttpResponseException {
                            switch(response.getStatusLine().getStatusCode()) {
                                case HttpStatus.SC_NO_CONTENT:
                                    return null;
                            }
                            throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                        }
                    });
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
        };
        return this.write(file, status, command);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        if(capabilities.extensions.contains(Extension.checksum)) {
            return ChecksumComputeFactory.get(capabilities.hashAlgorithm);
        }
        log.debug("No checksum support in capabilities {}", capabilities);
        return ChecksumComputeFactory.get(HashAlgorithm.sha1);
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.checksum, Flags.mime);
    }
}
