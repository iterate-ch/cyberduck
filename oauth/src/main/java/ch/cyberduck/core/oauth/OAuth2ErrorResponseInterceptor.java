package ch.cyberduck.core.oauth;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OAuth2ErrorResponseInterceptor extends DisabledServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(OAuth2ErrorResponseInterceptor.class);

    private final OAuth2RequestInterceptor service;

    public OAuth2ErrorResponseInterceptor(final Host host, final OAuth2RequestInterceptor service) {
        this.service = service;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_UNAUTHORIZED:
                try {
                    if(log.isWarnEnabled()) {
                        log.warn("Attempt to refresh OAuth tokens for failure {}", response);
                    }
                    service.save(service.refresh());
                    // Try again
                    return true;
                }
                catch(BackgroundException e) {
                    log.warn("Failure {} refreshing OAuth tokens", e);
                }
        }
        return false;
    }
}
