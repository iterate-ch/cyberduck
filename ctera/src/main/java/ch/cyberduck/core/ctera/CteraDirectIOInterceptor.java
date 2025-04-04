package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class CteraDirectIOInterceptor implements HttpRequestInterceptor {

    private static final Logger log = LogManager.getLogger(CteraDirectIOInterceptor.class);

    public static final String DIRECTIO_PATH = "/directio/";

    private final CteraSession session;

    public CteraDirectIOInterceptor(final CteraSession session) {
        this.session = session;
    }

    @Override
    public void process(final HttpRequest httpRequest, final HttpContext httpContext) throws HttpException, IOException {
        if(httpRequest.getRequestLine().getUri().startsWith(DIRECTIO_PATH)) {
            try {
                httpRequest.addHeader("Authorization", String.format("Bearer %s", session.getOrCreateAPIKeys().accessKey));
            }
            catch(BackgroundException e) {
                log.error("Failure creating API keys. {}", e.getMessage());
            }
        }
    }
}
