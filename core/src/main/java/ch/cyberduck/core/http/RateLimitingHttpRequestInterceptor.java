package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RateLimitingHttpRequestInterceptor implements HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(RateLimitingHttpRequestInterceptor.class);

    private final HttpRateLimiter limiter;

    public RateLimitingHttpRequestInterceptor(final HttpRateLimiter limiter) {
        this.limiter = limiter;
    }

    @Override
    public void process(final org.apache.http.HttpRequest request, final HttpContext context) {
        if(!limiter.tryAcquire()) {
            log.warn(String.format("Wait for rate limiting lock from %s", limiter));
            // Acquires a single permit blocking until the request can be granted
            final double time = limiter.acquire();
            if(log.isInfoEnabled()) {
                log.info(String.format("Proceed after waiting %s seconds from %s", time, limiter));
            }
        }
    }
}
