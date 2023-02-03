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

import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import synapticloop.b2.exception.B2ApiException;

public class B2ErrorResponseInterceptor extends DisabledServiceUnavailableRetryStrategy implements HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(B2ErrorResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    private String accountId = StringUtils.EMPTY;
    private String applicationKey = StringUtils.EMPTY;
    private String authorizationToken = StringUtils.EMPTY;

    public B2ErrorResponseInterceptor(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        if(executionCount <= MAX_RETRIES) {
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_UNAUTHORIZED:
                    final B2ApiException failure;
                    try {
                        if(null != response.getEntity()) {
                            EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                            failure = new B2ApiException(EntityUtils.toString(response.getEntity()), new HttpResponseException(
                                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
                            if(new B2ExceptionMappingService(fileid).map(failure) instanceof ExpiredTokenException) {
                                //  The authorization token is valid for at most 24 hours.
                                try {
                                    authorizationToken = session.getClient().authenticate(accountId, applicationKey).getAuthorizationToken();
                                    return true;
                                }
                                catch(B2ApiException | IOException e) {
                                    log.warn(String.format("Attempt to renew expired auth token failed. %s", e.getMessage()));
                                }
                            }
                        }
                    }
                    catch(IOException e) {
                        log.warn(String.format("Failure parsing response entity from %s", response));
                    }
            }
        }
        else {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Skip retry for response %s after %d executions", response, executionCount));
            }
        }
        return false;
    }

    public void setTokens(final String accountId, final String applicationKey, final String authorizationToken) {
        this.accountId = accountId;
        this.applicationKey = applicationKey;
        this.authorizationToken = authorizationToken;
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) {
        if(StringUtils.contains(request.getRequestLine().getUri(), "b2_authorize_account")) {
            // Skip setting token for
            if(log.isDebugEnabled()) {
                log.debug("Skip setting token in b2_authorize_account");
            }
            return;
        }
        switch(request.getRequestLine().getMethod()) {
            case "POST":
                // Do not override Authorization header for upload requests with upload URL token
                if(StringUtils.contains(request.getRequestLine().getUri(), "b2_upload_part")
                        || StringUtils.contains(request.getRequestLine().getUri(), "b2_upload_file")) {
                    break;
                }
            default:
                if(StringUtils.isNotBlank(authorizationToken)) {
                    request.removeHeaders(HttpHeaders.AUTHORIZATION);
                    request.addHeader(HttpHeaders.AUTHORIZATION, authorizationToken);
                }
        }
    }
}
