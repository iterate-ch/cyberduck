package ch.cyberduck.core.sts;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.s3.S3ExceptionMappingService;
import ch.cyberduck.core.s3.S3Session;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.security.AWSSessionCredentials;

import java.io.IOException;

public class STSAssumeRoleTokenExpiredResponseInterceptor extends OAuth2ErrorResponseInterceptor {
    private static final Logger log = LogManager.getLogger(STSAssumeRoleTokenExpiredResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final S3Session session;
    private final OAuth2RequestInterceptor oauth;
    private final STSAssumeRoleCredentialsRequestInterceptor sts;

    public STSAssumeRoleTokenExpiredResponseInterceptor(final S3Session session,
                                                        final OAuth2RequestInterceptor oauth,
                                                        final STSAssumeRoleCredentialsRequestInterceptor sts,
                                                        final LoginCallback prompt) {
        super(session.getHost(), oauth, prompt);
        this.session = session;
        this.oauth = oauth;
        this.sts = sts;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_UNAUTHORIZED:
                if(!super.retryRequest(response, executionCount, context)) {
                    return false;
                }
        }
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_BAD_REQUEST:
                try {
                    if(null != response.getEntity()) {
                        EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                        final S3ServiceException failure = new S3ServiceException(response.getStatusLine().getReasonPhrase(),
                                EntityUtils.toString(response.getEntity()));
                        failure.setResponseCode(response.getStatusLine().getStatusCode());
                        if(new S3ExceptionMappingService().map(failure) instanceof ExpiredTokenException) {
                            if(log.isWarnEnabled()) {
                                log.warn(String.format("Handle failure %s", failure));
                            }
                        }
                        else {
                            // Ignore other 400 failures
                            return false;
                        }
                    }
                }
                catch(IOException e) {
                    log.warn(String.format("Failure parsing response entity from %s", response));
                }
                // Break through
            case HttpStatus.SC_UNAUTHORIZED:
                if(executionCount <= MAX_RETRIES) {
                    try {
                        log.warn(String.format("Attempt to refresh STS token for failure %s", response));
                        final STSTokens tokens = sts.refresh(oauth.getTokens());
                        session.getClient().setProviderCredentials(new AWSSessionCredentials(tokens.getAccessKeyId(),
                                tokens.getSecretAccessKey(), tokens.getSessionToken()));
                        // Try again
                        return true;
                    }
                    catch(BackgroundException e) {
                        log.warn(String.format("Failure %s refreshing STS token", e));
                    }
                }
                else {
                    log.warn(String.format("Skip retry for response %s after %d executions", response, executionCount));
                }
                break;
        }
        return false;
    }
}