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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.util.concurrent.locks.ReentrantLock;

public class BrickUnauthorizedRetryStrategy extends OAuth2ErrorResponseInterceptor {
    private final Host host;
    private final CancelCallback cancel;
    private final ReentrantLock lock = new ReentrantLock();

    public BrickUnauthorizedRetryStrategy(final Host host, final BrickOAuth2AuthorizationService service, final CancelCallback cancel) {
        super(host, service, cancel);
        this.host = host;
        this.cancel = cancel;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        if(response.getStatusLine().getStatusCode() != HttpStatus.SC_UNAUTHORIZED
                || !Boolean.TRUE.equals(context.getAttribute(BrickOAuth2AuthorizationService.REST_ORIGIN))
                || host.getCredentials().isPasswordAuthentication()) {
            return false;
        }
        if(!lock.tryLock()) {
            return false;
        }
        try {
            cancel.verify();
            final HttpRequest request = HttpCoreContext.adapt(context).getRequest();
            final Header sent = request == null ? null : request.getFirstHeader(BrickOAuth2AuthorizationService.API_KEY_HEADER);
            final String current = host.getCredentials().getOauth().getAccessToken();
            if(StringUtils.isNotBlank(current) && sent != null && !StringUtils.equals(sent.getValue(), current)) {
                return true;
            }
            return super.retryRequest(response, executionCount, context);
        }
        catch(ConnectionCanceledException e) {
            return false;
        }
        finally {
            lock.unlock();
        }
    }
}
