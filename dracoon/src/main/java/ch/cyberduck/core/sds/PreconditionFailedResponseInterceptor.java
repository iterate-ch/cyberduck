package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreconditionFailedResponseInterceptor extends OAuth2ErrorResponseInterceptor {
    private static final Logger log = LogManager.getLogger(PreconditionFailedResponseInterceptor.class);

    private final OAuth2RequestInterceptor service;

    public PreconditionFailedResponseInterceptor(final Host bookmark,
                                                 final OAuth2RequestInterceptor service,
                                                 final LoginCallback prompt) {
        super(bookmark, service);
        this.service = service;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_PRECONDITION_FAILED:
                try {
                    log.warn("Invalidate OAuth tokens due to failed precondition {}", response);
                    service.save(service.authorize());
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
