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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AntiVirusAccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadTokenGenerateResponse;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class SDSReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(SDSReadFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSReadFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final SDSApiClient client = session.getClient();
            final DownloadTokenGenerateResponse token = new NodesApi(session.getClient()).generateDownloadUrl(Long.valueOf(nodeid.getVersionId(file)), StringUtils.EMPTY);
            final HttpUriRequest request = new HttpGet(token.getDownloadUrl());
            request.addHeader("X-Sds-Auth-Token", StringUtils.EMPTY);
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                final String header;
                if(TransferStatus.UNKNOWN_LENGTH == range.getEnd()) {
                    header = String.format("bytes=%d-", range.getStart());
                }
                else {
                    header = String.format("bytes=%d-%d", range.getStart(), range.getEnd());
                }
                log.debug("Add range header {} for file {}", header, file);
                request.addHeader(new BasicHeader(HttpHeaders.RANGE, header));
                // Disable compression
                request.addHeader(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "identity"));
            }
            final HttpResponse response = client.getClient().execute(request);
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_PARTIAL_CONTENT:
                    return new HttpMethodReleaseInputStream(response, status);
                case HttpStatus.SC_NOT_FOUND:
                    nodeid.cache(file, null);
                    // Break through
                default:
                    throw new DefaultHttpResponseExceptionMappingService().map("Download {0} failed", new HttpResponseException(
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()), file);
            }
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        final PathAttributes attr = file.attributes();
        if(null != attr.getVerdict()) {
            switch(attr.getVerdict()) {
                case malicious:
                    throw new AntiVirusAccessDeniedException().withFile(file);
            }
        }
    }
}
