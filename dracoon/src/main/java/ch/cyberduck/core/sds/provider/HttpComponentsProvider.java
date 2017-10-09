package ch.cyberduck.core.sds.provider;

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

import org.apache.http.impl.client.CloseableHttpClient;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

public class HttpComponentsProvider implements ConnectorProvider {

    private final CloseableHttpClient apache;

    public HttpComponentsProvider(final CloseableHttpClient apache) {
        this.apache = apache;
    }

    @Override
    public Connector getConnector(final Client client, final Configuration runtime) {
        return new HttpComponentsConnector(apache, runtime);
    }
}
