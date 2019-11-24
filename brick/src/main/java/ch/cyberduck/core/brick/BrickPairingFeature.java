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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Pairing;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.log4j.Logger;

import java.io.IOException;

public class BrickPairingFeature implements Pairing {
    private static final Logger log = Logger.getLogger(BrickPairingFeature.class);

    private final BrickSession session;

    public BrickPairingFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public void delete(final String token) throws BackgroundException {
        try {
            final HttpRequestBase resource = new HttpDelete("https://app.files.com/api/rest/v1/api_key");
            resource.setHeader("X-FilesAPI-Auth", token);
            resource.setHeader(HttpHeaders.ACCEPT, "application/json");
            resource.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            if(log.isInfoEnabled()) {
                log.info(String.format("Delete paring key %s", token));
            }
            session.getClient().execute(resource, new ResponseHandler<Void>() {
                @Override
                public Void handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    return null;
                }
            });
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
