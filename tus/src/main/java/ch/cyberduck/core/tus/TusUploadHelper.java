package ch.cyberduck.core.tus;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpHead;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static ch.cyberduck.core.tus.TusCapabilities.*;
import static ch.cyberduck.core.tus.TusCapabilities.TUS_HEADER_UPLOAD_OFFSET;

public class TusUploadHelper {
    private static final Logger log = LogManager.getLogger(TusUploadHelper.class);

    private final Session<DAVClient> session;

    public TusUploadHelper(Session<DAVClient> session) {
        this.session = session;
    }

    /**
     * Query the current upload offset for the resource to confirm completion of a request timing out
     * while waiting for the response
     *
     * @return The upload offset
     */
    public long offset(final String uploadUrl) throws BackgroundException {
        final HttpHead request = new HttpHead(uploadUrl);
        request.setHeader(TUS_HEADER_RESUMABLE, TUS_VERSION);
        try {
            return session.getClient().execute(request, new ResponseHandler<Long>() {
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
}
