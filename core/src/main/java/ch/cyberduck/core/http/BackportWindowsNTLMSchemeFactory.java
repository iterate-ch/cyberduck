package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.protocol.HttpContext;

/**
 * Backport of HTTPCLIENT-1947
 */
public class BackportWindowsNTLMSchemeFactory implements AuthSchemeProvider {

    private final String servicePrincipalName;

    public BackportWindowsNTLMSchemeFactory(final String servicePrincipalName) {
        super();
        this.servicePrincipalName = servicePrincipalName;
    }

    @Override
    public AuthScheme create(final HttpContext context) {
        return new BackportWindowsNegotiateScheme(AuthSchemes.NTLM, servicePrincipalName);
    }
}
