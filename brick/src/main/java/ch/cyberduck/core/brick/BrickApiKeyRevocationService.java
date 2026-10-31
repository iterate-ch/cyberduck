package ch.cyberduck.core.brick;

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

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

import java.io.IOException;
import java.net.URI;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;

public class BrickApiKeyRevocationService {
    private final HttpTransport transport;

    public BrickApiKeyRevocationService(final HttpTransport transport) {
        this.transport = transport;
    }

    public void revoke(final URI server, final String apiKey) throws IOException {
        final HttpRequest request = transport.createRequestFactory().buildDeleteRequest(
                new GenericUrl(server.resolve("/api/rest/v1/api_key").toString()));
        request.getHeaders().set("X-FilesAPI-Key", apiKey).setAccept("application/json");
        request.setFollowRedirects(false).setThrowExceptionOnExecuteError(false);
        final HttpResponse response = request.execute();
        try {
            final int status = response.getStatusCode();
            if(!response.isSuccessStatusCode() && status != HttpStatus.SC_UNAUTHORIZED && status != HttpStatus.SC_NOT_FOUND) {
                throw new HttpResponseException(status, response.getStatusMessage());
            }
        }
        finally {
            response.disconnect();
        }
    }
}
