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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VoidAttributesAdapter;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.EnumSet;

import static ch.cyberduck.core.tus.TusCapabilities.*;

public class TusWriteFeature extends AbstractHttpWriteFeature<Void> {
    private static final Logger log = LogManager.getLogger(TusWriteFeature.class);

    private final Host host;
    private final TusCapabilities capabilities;
    private final DAVClient client;

    public TusWriteFeature(final Host host, final TusCapabilities capabilities, final DAVClient client) {
        super(host, new VoidAttributesAdapter());
        this.host = host;
        this.capabilities = capabilities;
        this.client = client;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DelayedHttpEntityCallable<Void> command = new DelayedHttpEntityCallable<Void>(file) {
            @Override
            public Void call(final HttpEntity entity) throws BackgroundException {
                final HttpPatch request = new HttpPatch(status.getParameters().get(TusUploadFeature.UPLOAD_URL).toString());
                request.setEntity(entity);
                request.setHeader(TUS_HEADER_RESUMABLE, TUS_VERSION);
                final Checksum checksum = status.getChecksum();
                if(Checksum.NONE != checksum) {
                    request.setHeader(TUS_HEADER_UPLOAD_CHECKSUM, String.format("%s %s", checksum.algorithm, checksum.hex));
                }
                request.setHeader(TUS_HEADER_UPLOAD_OFFSET, String.valueOf(status.getOffset()));
                // All PATCH requests MUST use Content-Type: application/offset+octet-stream
                request.setHeader(HttpHeaders.CONTENT_TYPE, "application/offset+octet-stream");
                // Last chunk completing the upload. The server may take a significant amount of time to respond
                // while assembling and validating previously uploaded chunks
                final boolean finalize = (status.getOffset() + status.getLength()) == status.getParent().getLength();
                if(finalize) {
                    final RequestConfig context = client.getContext().getRequestConfig();
                    request.setConfig(RequestConfig.copy(context)
                            .setSocketTimeout(1000 * HostPreferencesFactory.get(host).getInteger("tus.upload.finalize.timeout"))
                            .build());
                }
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
                catch(SocketTimeoutException e) {
                    if(finalize) {
                        log.warn("Timeout waiting for response completing upload of {} to {}", file, request.getURI(), e);
                        try {
                            if(status.getParent().getLength() == TusWriteFeature.this.offset(request.getURI().toString())) {
                                log.info("Confirmed upload of {} is complete querying offset for {} after timeout waiting for response",
                                        file, request.getURI());
                                return null;
                            }
                        }
                        catch(BackgroundException f) {
                            log.warn("Failure {} querying offset for {} to confirm completion of {}", f, request.getURI(), file);
                        }
                    }
                    throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
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

    /**
     * Query the current upload offset for the resource to confirm completion of a request timing out
     * while waiting for the response
     *
     * @return The upload offset
     */
    private long offset(final String uploadUrl) throws BackgroundException {
        final HttpHead request = new HttpHead(uploadUrl);
        request.setHeader(TUS_HEADER_RESUMABLE, TUS_VERSION);
        try {
            return client.execute(request, new ResponseHandler<Long>() {
                @Override
                public Long handleResponse(final HttpResponse response) throws HttpResponseException {
                    switch(response.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_OK:
                            if(response.containsHeader(TUS_HEADER_UPLOAD_OFFSET)) {
                                final Header header = response.getFirstHeader(TUS_HEADER_UPLOAD_OFFSET);
                                log.debug("Return offset header {}", header);
                                return Long.valueOf(header.getValue());
                            }
                    }
                    // No Upload-Offset response header
                    throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                }
            });
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map("Upload failed", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e);
        }
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
