package ch.cyberduck.core.http;

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

import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

public class DisabledServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy {
    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        return false;
    }

    @Override
    public long getRetryInterval() {
        return 0L;
    }
}
