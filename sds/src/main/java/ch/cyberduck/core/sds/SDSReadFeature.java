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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class SDSReadFeature implements Read {
    private static final Logger log = Logger.getLogger(SDSReadFeature.class);

    private final SDSSession session;

    public SDSReadFeature(final SDSSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final SDSApiClient client = session.getClient();
            final HttpUriRequest request = new HttpGet(String.format("%s/nodes/files/%s/downloads", client.getBasePath(),
                    new SDSNodeIdProvider(session).getFileid(file, new DisabledListProgressListener())));
            request.addHeader("X-Sds-Auth-Token", StringUtils.EMPTY);
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                final String header;
                if(-1 == range.getEnd()) {
                    header = String.format("bytes=%d-", range.getStart());
                }
                else {
                    header = String.format("bytes=%d-%d", range.getStart(), range.getEnd());
                }
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Add range header %s for file %s", header, file));
                }
                request.addHeader(new BasicHeader(HttpHeaders.RANGE, header));
                // Disable compression
                request.addHeader(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "identity"));
            }
            final HttpResponse response = client.getClient().execute(request);
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_PARTIAL_CONTENT:
                    return new HttpMethodReleaseInputStream(response);
                default:
                    throw new HttpResponseExceptionMappingService().map(new HttpResponseException(
                            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return true;
    }
}
