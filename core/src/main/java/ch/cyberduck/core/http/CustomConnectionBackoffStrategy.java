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

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultBackoffStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomConnectionBackoffStrategy extends DefaultBackoffStrategy {
    private static final Logger log = LogManager.getLogger(CustomConnectionBackoffStrategy.class);

    @Override
    public boolean shouldBackoff(final Throwable t) {
        return false;
    }

    @Override
    public boolean shouldBackoff(final HttpResponse response) {
        final boolean backoff = super.shouldBackoff(response);
        if(backoff) {
            log.warn(String.format("Backoff for reply %s", response));
        }
        return backoff;
    }
}
