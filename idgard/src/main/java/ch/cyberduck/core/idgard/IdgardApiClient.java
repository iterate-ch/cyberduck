package ch.cyberduck.core.idgard;

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

import ch.cyberduck.core.idgard.io.swagger.client.ApiClient;

import org.apache.http.impl.client.CloseableHttpClient;

import javax.ws.rs.client.Client;
import java.util.logging.Logger;

public class IdgardApiClient extends ApiClient {

    static {
        Logger.getLogger("org.glassfish.jersey.client.ClientExecutorProvidersConfigurator").setLevel(java.util.logging.Level.SEVERE);
    }

    private final CloseableHttpClient client;

    public IdgardApiClient(final CloseableHttpClient client) {
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
}
