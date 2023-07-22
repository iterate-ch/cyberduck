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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;
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

public class S3WebIdentityTokenExpiredResponseInterceptor extends DisabledServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(S3WebIdentityTokenExpiredResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final S3Session session;
    private final Host host;
    private final STSCredentialsRequestInterceptor authorizationService;
    private final LoginCallback prompt;

    public S3WebIdentityTokenExpiredResponseInterceptor(final S3Session session, final LoginCallback prompt,
                                                        STSCredentialsRequestInterceptor authorizationService) {
        this.session = session;
        this.host = session.getHost();
        this.authorizationService = authorizationService;
        this.prompt = prompt;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        if(executionCount <= MAX_RETRIES) {
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_BAD_REQUEST:
                case HttpStatus.SC_FORBIDDEN:
                    try {
                        final S3ServiceException failure;
                        if(null != response.getEntity()) {
                            EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                            failure = new S3ServiceException(response.getStatusLine().getReasonPhrase(),
                                    EntityUtils.toString(response.getEntity()));
                        }
                        // In case of a http HEAD request minio packs the error code and description in the response header
                        else {
                            failure = new S3ServiceException(response.getStatusLine().getReasonPhrase());
                            if(response.containsHeader("x-minio-error-code")) {
                                failure.setErrorCode(response.getFirstHeader("x-minio-error-code").getValue());
                            }
                            if(response.containsHeader("x-minio-error-desc")) {
                                failure.setErrorMessage(response.getFirstHeader("x-minio-error-desc").getValue());
                            }
                        }

                        failure.setResponseCode(response.getStatusLine().getStatusCode());
                        BackgroundException s3exception = new S3ExceptionMappingService().map(failure);

                        if(failure.getErrorCode().equals("InvalidAccessKeyId") || s3exception instanceof ExpiredTokenException) {
                            refreshOAuthAndSTS();
                        }
                        return true;
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

    private void refreshOAuthAndSTS() {
        try {
            try {
                authorizationService.save(authorizationService.refresh());
                log.debug("OAuth refreshed. Refreshing STS token.");
            }
            catch(InteroperabilityException | LoginFailureException e3) {
                log.warn(String.format("Failure %s refreshing OAuth tokens", e3));
                authorizationService.save(authorizationService.authorize(host, prompt, new DisabledCancelCallback()));
            }
            try {
                Credentials credentials = authorizationService.assumeRoleWithWebIdentity();
                session.getClient().setProviderCredentials(credentials.isAnonymousLogin() ? null :
                        new AWSSessionCredentials(credentials.getUsername(), credentials.getPassword(),
                                credentials.getToken()));
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure %s fetching temporary STS credentials with oAuth token", e));
            }
        }
        catch(BackgroundException e) {
            log.warn(String.format("Failure %s refreshing OAuth tokens", e));
        }
    }
}