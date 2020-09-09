package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

public class BrickUnauthorizedRetryStrategy extends DisabledServiceUnavailableRetryStrategy {
    private static final Logger log = Logger.getLogger(BrickUnauthorizedRetryStrategy.class);

    private static final int MAX_RETRIES = 1;

    private final BrickSession session;
    private final LoginCallback prompt;

    public BrickUnauthorizedRetryStrategy(final BrickSession session, final LoginCallback prompt) {
        this.session = session;
        this.prompt = prompt;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_UNAUTHORIZED:
                if(executionCount <= MAX_RETRIES) {
                    // Pairing token no longer valid
                    try {
                        session.pair(session.getHost(), prompt, new DisabledCancelCallback()).setSaved(true);
                        return true;
                    }
                    catch(BackgroundException e) {
                        log.warn(String.format("Failure %s trying to refresh pairing after error response %s", e, response));
                    }
                }
        }
        return false;
    }
}
