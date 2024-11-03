package ch.cyberduck.core.http;/*
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
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChainedServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(ChainedServiceUnavailableRetryStrategy.class);

    private final ServiceUnavailableRetryStrategy[] chain;

    public ChainedServiceUnavailableRetryStrategy(final ServiceUnavailableRetryStrategy... chain) {
        this.chain = chain;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        for(ServiceUnavailableRetryStrategy strategy : chain) {
            if(strategy.retryRequest(response, executionCount, context)) {
                if(log.isWarnEnabled()) {
                    log.warn("Retry for response {} determined by {}", response, strategy);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public long getRetryInterval() {
        for(ServiceUnavailableRetryStrategy strategy : chain) {
            return strategy.getRetryInterval();
        }
        return 0L;
    }
}
