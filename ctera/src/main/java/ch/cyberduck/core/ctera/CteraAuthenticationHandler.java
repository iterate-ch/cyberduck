package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ctera.auth.CteraTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpExceptionMappingService;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class CteraAuthenticationHandler implements ServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(CteraAuthenticationHandler.class);

    private final CteraSession session;

    private CteraTokens tokens = CteraTokens.EMPTY;

    public CteraAuthenticationHandler(final CteraSession session) {
        this.session = session;
    }

    public void setTokens(final CteraTokens tokens) {
        this.tokens = tokens;
    }

    public void authenticate() throws BackgroundException {
        final HttpPost login = new HttpPost("/ServicesPortal/api/login?format=jsonext");
        try {
            login.setEntity(
                    new StringEntity(String.format("j_username=device%%5c%s&j_password=%s", tokens.getDeviceId(), tokens.getSharedSecret()),
                            ContentType.APPLICATION_FORM_URLENCODED
                    )
            );
            session.getClient().execute(login, new AbstractResponseHandler<Void>() {
                @Override
                public Void handleResponse(final HttpResponse response) throws IOException {
                    if(!response.containsHeader("Set-Cookie")) {
                        log.warn(String.format("No cookie in response %s", response));
                    }
                    else {
                        final Header header = response.getFirstHeader("Set-Cookie");
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Received cookie %s", header));
                        }
                    }
                    return super.handleResponse(response);
                }

                @Override
                public Void handleEntity(final HttpEntity entity) {
                    return null;
                }
            });
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_MOVED_TEMPORARILY:
                try {
                    log.info(String.format("Attempt to refresh cookie for failure %s", response));
                    this.authenticate();
                    // Try again
                    return true;
                }
                catch(BackgroundException e) {
                    log.error(String.format("Failure refreshing cookie. %s", e));
                    return false;
                }
        }
        return false;
    }

    @Override
    public long getRetryInterval() {
        return 0L;
    }
}
