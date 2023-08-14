package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSSessionCredentials;

import java.io.IOException;

/**
 * Update credentials on authentication failure
 */
public class S3AuthenticationResponseInterceptor extends DisabledServiceUnavailableRetryStrategy implements S3CredentialsStrategy {
    private static final Logger log = LogManager.getLogger(S3AuthenticationResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final S3Session session;
    private final S3CredentialsStrategy authenticator;

    public S3AuthenticationResponseInterceptor(final S3Session session, final S3CredentialsStrategy authenticator) {
        this.session = session;
        this.authenticator = authenticator;
    }

    @Override
    public Credentials get() throws BackgroundException {
        return authenticator.get();
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        if(executionCount <= MAX_RETRIES) {
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_FORBIDDEN:
                case HttpStatus.SC_BAD_REQUEST:
                    try {
                        final BackgroundException failure = new S3ExceptionMappingService().map(response);
                        if(failure instanceof LoginFailureException) {
                            // 403 Forbidden (InvalidAccessKeyId) The provided token has expired
                            // 400 Bad Request (ExpiredToken) The provided token has expired
                            // 400 Bad Request (InvalidToken) The provided token is malformed or otherwise not valid
                            // 400 Bad Request (TokenRefreshRequired) The provided token must be refreshed.
                            if(log.isWarnEnabled()) {
                                log.warn(String.format("Handle failure %s from response %s", failure, response));
                            }
                            final Credentials credentials = authenticator.get();
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Reconfigure client with credentials %s", credentials));
                            }
                            if(credentials.getTokens().validate()) {
                                session.getClient().setProviderCredentials(new AWSSessionCredentials(
                                        credentials.getTokens().getAccessKeyId(), credentials.getTokens().getSecretAccessKey(),
                                        credentials.getTokens().getSessionToken()));
                            }
                            else {
                                session.getClient().setProviderCredentials(new AWSCredentials(
                                        credentials.getUsername(), credentials.getPassword()));
                            }
                            return true;
                        }
                    }
                    catch(IOException e) {
                        log.warn(String.format("Failure parsing response entity from %s", response));
                    }
                    catch(BackgroundException e) {
                        log.warn(String.format("Failure %s retrieving credentials", e));
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
}
