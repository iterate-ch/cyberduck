package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.UseragentProvider;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

public class UserAgentHttpRequestInitializer implements HttpRequestInitializer {

    private final UseragentProvider provider;

    public UserAgentHttpRequestInitializer(final UseragentProvider provider) {
        this.provider = provider;
    }

    @Override
    public void initialize(final HttpRequest request) {
        request.getHeaders().setUserAgent(provider.get());
        request.getHeaders().setAccept("application/json");
        request.setSuppressUserAgentSuffix(true);
    }
}
