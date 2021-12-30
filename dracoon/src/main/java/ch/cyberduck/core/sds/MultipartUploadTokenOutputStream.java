package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class MultipartUploadTokenOutputStream extends OutputStream {
    private static final Logger log = LogManager.getLogger(MultipartUploadTokenOutputStream.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final Path file;
    private final TransferStatus overall;
    private final String uploadUrl;
    private final AtomicReference<BackgroundException> canceled = new AtomicReference<>();

    private Long offset = 0L;
    private final Long length;

    public MultipartUploadTokenOutputStream(final SDSSession session, final SDSNodeIdProvider nodeid, final Path file, final TransferStatus status, final String uploadUrl) {
        this.session = session;
        this.nodeid = nodeid;
        this.file = file;
        this.uploadUrl = uploadUrl;
        this.overall = status;
        this.length = status.getOffset() + status.getLength();
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
            final HttpEntity entity = EntityBuilder.create().setBinary(content).build();
            new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<Void>() {
                @Override
                public Void call() throws BackgroundException {
                    final SDSApiClient client = session.getClient();
                    try {
                        final HttpPost request = new HttpPost(uploadUrl);
                        request.setEntity(entity);
                        request.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeService.DEFAULT_CONTENT_TYPE);
                        request.setHeader(SDSSession.SDS_AUTH_TOKEN_HEADER, StringUtils.EMPTY);
                        if(0L != overall.getLength() && 0 != content.length) {
                            final HttpRange range = HttpRange.byLength(offset, content.length);
                            final String header;
                            if(overall.getLength() == TransferStatus.UNKNOWN_LENGTH) {
                                header = String.format("%d-%d/*", range.getStart(), range.getEnd());
                            }
                            else {
                                header = String.format("%d-%d/%d", range.getStart(), range.getEnd(), length);
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
                                    throw new SDSExceptionMappingService(nodeid).map(
                                        new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
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
                    catch(HttpResponseException e) {
                        throw new DefaultHttpResponseExceptionMappingService().map(e);
                    }
                    catch(IOException e) {
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("MultipartUploadTokenOutputStream{");
        sb.append("uploadUrl='").append(uploadUrl).append('\'');
        sb.append(", file=").append(file);
        sb.append(", offset=").append(offset);
        sb.append(", length=").append(length);
        sb.append('}');
        return sb.toString();
    }
}
