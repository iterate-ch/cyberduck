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
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomServiceUnavailableRetryStrategy extends ChainedServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(CustomServiceUnavailableRetryStrategy.class);

    private final Host host;

    public CustomServiceUnavailableRetryStrategy(final Host host) {
        this(host, new DisabledServiceUnavailableRetryStrategy());
    }

    public CustomServiceUnavailableRetryStrategy(final Host host, final ServiceUnavailableRetryStrategy... chain) {
        this(host, new HostPreferences(host).getInteger("connection.retry"), chain);
    }

    public CustomServiceUnavailableRetryStrategy(final Host host, final int executionCount, final ServiceUnavailableRetryStrategy... chain) {
        super(new ChainedServiceUnavailableRetryStrategy(new ExecutionCountServiceUnavailableRetryStrategy(
                executionCount, new DefaultServiceUnavailableRetryStrategy(host)),
                new ChainedServiceUnavailableRetryStrategy(chain)));
        this.host = host;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        // Proxy to chain
        if(super.retryRequest(response, executionCount, context)) {
            log.warn("Allow retry for response {} if repeatable", response);
            return true;
        }
        return false;
    }

    @Override
    public long getRetryInterval() {
        return new HostPreferences(host).getLong("connection.retry.delay") * 1000L;
    }
}
