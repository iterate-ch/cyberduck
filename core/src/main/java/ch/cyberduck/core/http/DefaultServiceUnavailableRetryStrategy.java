package ch.cyberduck.core.http;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(DefaultServiceUnavailableRetryStrategy.class);

    private final Host host;

    public DefaultServiceUnavailableRetryStrategy(final Host host) {
        this.host = host;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        final boolean retry = this.evaluate(response);
        if(retry) {
            if(log.isWarnEnabled()) {
                log.warn("Allow retry for response {} if repeatable", response);
            }
        }
        return retry;
    }

    @Override
    public long getRetryInterval() {
        return new HostPreferences(host).getLong("connection.retry.delay") * 1000L;
    }

    /**
     * @param response Server response
     * @return True if request should be retried given the HTTP response
     */
    protected boolean evaluate(final HttpResponse response) {
        // Apply default if not handled
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
            case HttpStatus.SC_GATEWAY_TIMEOUT:
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            case HttpStatus.SC_REQUEST_TIMEOUT:
                return true;
        }
        return false;
    }

}
