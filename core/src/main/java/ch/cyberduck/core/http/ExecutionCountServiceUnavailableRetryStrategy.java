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

public class ExecutionCountServiceUnavailableRetryStrategy extends ChainedServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(ExecutionCountServiceUnavailableRetryStrategy.class);

    private static final int DEFAULT_MAX_RETRIES = 1;

    private final int maxExecutionCount;

    public ExecutionCountServiceUnavailableRetryStrategy(final ServiceUnavailableRetryStrategy... chain) {
        this(DEFAULT_MAX_RETRIES, chain);
    }

    public ExecutionCountServiceUnavailableRetryStrategy(int maxExecutionCount, final ServiceUnavailableRetryStrategy... chain) {
        super(chain);
        this.maxExecutionCount = maxExecutionCount;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        if(executionCount > maxExecutionCount) {
            if(log.isWarnEnabled()) {
                log.warn("Skip retry for response {} after {} executions", response, executionCount);
            }
            return false;
        }
        return super.retryRequest(response, executionCount, context);
    }
}
