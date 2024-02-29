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

import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;

public class CustomServiceUnavailableRetryStrategy extends ChainedServiceUnavailableRetryStrategy {

    private final Host host;

    public CustomServiceUnavailableRetryStrategy(final Host host) {
        this(host, new DisabledServiceUnavailableRetryStrategy());
    }

    public CustomServiceUnavailableRetryStrategy(final Host host, final ServiceUnavailableRetryStrategy proxy) {
        super(proxy, new DefaultServiceUnavailableRetryStrategy(new HostPreferences(host).getInteger("http.connections.retry"),
                new HostPreferences(host).getInteger("http.connections.retry.interval")));
        this.host = host;
    }

    @Override
    public long getRetryInterval() {
        return new HostPreferences(host).getInteger("http.connections.retry.interval");
    }
}
