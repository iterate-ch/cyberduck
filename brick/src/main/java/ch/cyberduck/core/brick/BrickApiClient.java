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

import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.JSON;
import ch.cyberduck.core.brick.io.swagger.client.Pair;
import ch.cyberduck.core.jersey.HttpComponentsProvider;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BrickApiClient extends ApiClient {

    static {
        Logger.getLogger("org.glassfish.jersey.client.ClientExecutorProvidersConfigurator").setLevel(java.util.logging.Level.SEVERE);
    }

    private final BrickSession session;

    public BrickApiClient(final BrickSession session) {
        this.session = session;
        this.setHttpClient(ClientBuilder.newClient(new ClientConfig()
                .register(new InputStreamProvider())
                .register(MultiPartFeature.class)
                .register(new JSON())
                .register(JacksonFeature.class)
                .connectorProvider(new HttpComponentsProvider(session.getClient())))
        );
        final int timeout = ConnectionTimeoutFactory.get().getTimeout() * 1000;
        this.setConnectTimeout(timeout);
        this.setReadTimeout(timeout);
        this.setUserAgent(new PreferencesUseragentProvider().get());
        this.setBasePath("https://app.files.com/api/rest/v1");
    }

    @Override
    protected Client buildHttpClient(final boolean debugging) {
        // No need to build default client
        return null;
    }

    @Override
    public <T> T invokeAPI(final String path, final String method, final List<Pair> queryParams, final Object body, final Map<String, String> headerParams, final Map<String, Object> formParams, final String accept, final String contentType, final String[] authNames, final GenericType<T> returnType) throws ApiException {
        try {
            this.setBasePath(String.format("%s/api/rest/v1", new HostUrlProvider().withUsername(false).get(session.getHost())));
            return super.invokeAPI(path, method, queryParams, body, headerParams, formParams, accept, contentType, authNames, returnType);
        }
        catch(ProcessingException e) {
            throw new ApiException(e);
        }
    }

    @Override
    protected void updateParamsForAuth(final String[] authNames, final List<Pair> queryParams, final Map<String, String> headerParams) {
        // Handled in interceptor
    }
}
