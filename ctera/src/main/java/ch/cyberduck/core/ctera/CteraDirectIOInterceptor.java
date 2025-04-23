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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class CteraDirectIOInterceptor implements HttpRequestInterceptor, ServiceUnavailableRetryStrategy {
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
                log.error("Failure {} creating API keys", e.toString());
                throw new IOException(e);
            }
        }
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_UNAUTHORIZED:
                final HttpClientContext clientContext = HttpClientContext.adapt(context);
                if(StringUtils.startsWith(clientContext.getRequest().getRequestLine().getUri(), CteraDirectIOInterceptor.DIRECTIO_PATH)) {
                    try {
                        session.createAPICredentials();
                        // Try again
                        return true;
                    }
                    catch(BackgroundException e) {
                        log.error("Failure {} creating API keys", e.toString());
                        return false;
                    }
                }
        }
        return false;
    }

    @Override
    public long getRetryInterval() {
        return 0L;
    }
}
