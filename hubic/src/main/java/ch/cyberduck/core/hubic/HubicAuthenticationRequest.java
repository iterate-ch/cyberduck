package ch.cyberduck.core.hubic;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;

import java.net.URI;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.method.AuthenticationRequest;

public class HubicAuthenticationRequest extends HttpGet implements AuthenticationRequest {

    public HubicAuthenticationRequest(final String token) {
        this(URI.create("https://api.hubic.com/1.0/account/credentials"), token);
    }

    public HubicAuthenticationRequest(final URI uri, final String token) {
        super(uri);
        this.setHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));
    }

    @Override
    public Client.AuthVersion getVersion() {
        return Client.AuthVersion.v10;
    }
}
