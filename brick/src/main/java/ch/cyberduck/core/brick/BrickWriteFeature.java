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
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class BrickWriteFeature extends AbstractHttpWriteFeature<Void> {
    private static final Logger log = Logger.getLogger(BrickWriteFeature.class);

    private final BrickSession session;

    public BrickWriteFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return this.write(file, status, new DelayedHttpEntityCallable<Void>() {
            @Override
            public Void call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final HttpPut request = new HttpPut(status.getUrl());
                    request.setEntity(entity);
                    request.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeService.DEFAULT_CONTENT_TYPE);
                    final HttpResponse response = session.getClient().execute(request);
                    // Validate response
                    try {
                        switch(response.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                                // Upload complete
                                if(response.containsHeader("ETag")) {
                                    if(log.isInfoEnabled()) {
                                        log.info(String.format("Received response %s for part number %d", response, status.getPart()));
                                    }
                                    status.setChecksum(Checksum.parse(StringUtils.remove(response.getFirstHeader("ETag").getValue(), '"')));
                                    return null;
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
                catch(HttpResponseException e) {
                    throw new DefaultHttpResponseExceptionMappingService().map(e);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        });
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new MD5ChecksumCompute();
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean timestamp() {
        return true;
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }
}
