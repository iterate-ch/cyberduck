package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

import synapticloop.b2.exception.B2ApiException;

public class B2ErrorResponseInterceptor extends DisabledServiceUnavailableRetryStrategy {
    private static final Logger log = Logger.getLogger(B2ErrorResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final B2Session session;

    private String accountId = StringUtils.EMPTY;
    private String applicationKey = StringUtils.EMPTY;

    public B2ErrorResponseInterceptor(final B2Session session) {
        this.session = session;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_UNAUTHORIZED:
                if(executionCount <= MAX_RETRIES) {
                    final B2ApiException failure;
                    try {
                        EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                        failure = new B2ApiException(EntityUtils.toString(response.getEntity()), new HttpResponseException(
                                response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
                    }
                    catch(IOException e) {
                        log.warn(String.format("Failure parsing response entity from %s", response));
                        return false;
                    }
                    if("expired_auth_token".equalsIgnoreCase(failure.getCode())) {
                        //  The authorization token is valid for at most 24 hours.
                        try {
                            session.getClient().authenticate(accountId, applicationKey);
                            return true;
                        }
                        catch(B2ApiException | IOException e) {
                            log.warn(String.format("Attempt to renew expired auth token failed. %s", e.getMessage()));
                            return false;
                        }
                    }
                }
        }
        return false;
    }

    public void setTokens(final String accountId, final String applicationKey) {
        this.accountId = accountId;
        this.applicationKey = applicationKey;
    }
}
