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

import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.http.HttpConnectionPoolBuilder;

import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

public class HttpComponentsProvider implements ConnectorProvider {

    private final HttpConnectionPoolBuilder builder;
    private final TranscriptListener transcript;

    public HttpComponentsProvider(final HttpConnectionPoolBuilder builder, final TranscriptListener transcript) {
        this.builder = builder;
        this.transcript = transcript;
    }

    @Override
    public Connector getConnector(final Client client, final Configuration runtimeConfig) {
        return new HttpComponentsConnector(builder, transcript, client, runtimeConfig);
    }
}
