package ch.cyberduck.core.brick;

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

import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import org.apache.http.impl.client.CloseableHttpClient;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Map;

public class BrickApiClient extends ApiClient {

    private final CloseableHttpClient client;

    public BrickApiClient(final CloseableHttpClient client) {
        this.client = client;
    }

    public CloseableHttpClient getClient() {
        return client;
    }

    @Override
    protected Client buildHttpClient(final boolean debugging) {
        // No need to build default client
        return null;
    }

    @Override
    public <T> T invokeAPI(final String path, final String method, final List<Pair> queryParams, final Object body, final Map<String, String> headerParams, final Map<String, Object> formParams, final String accept, final String contentType, final String[] authNames, final GenericType<T> returnType) throws ApiException {
        try {
            return super.invokeAPI(path, method, queryParams, body, headerParams, formParams, accept, contentType,
                new String[]{"api_key"}, returnType);
        }
        catch(ProcessingException e) {
            throw new ApiException(e);
        }
    }
}
