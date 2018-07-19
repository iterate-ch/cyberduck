package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;
import ch.cyberduck.core.sts.STSCredentialsConfigurator;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.S3ServiceException;

import java.io.IOException;

public class S3TokenExpiredResponseInterceptor extends DisabledServiceUnavailableRetryStrategy {
    private static final Logger log = Logger.getLogger(S3TokenExpiredResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final S3Session session;
    private final LoginCallback prompt;

    public S3TokenExpiredResponseInterceptor(final S3Session session, final LoginCallback prompt) {
        this.session = session;
        this.prompt = prompt;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        switch(response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_BAD_REQUEST:
                if(executionCount <= MAX_RETRIES) {
                    final S3ServiceException failure;
                    try {
                        EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                        failure = new S3ServiceException(response.getStatusLine().getReasonPhrase(),
                            EntityUtils.toString(response.getEntity()));
                        if(new S3ExceptionMappingService().map(failure) instanceof ExpiredTokenException) {
                            try {
                                final Credentials auto = new STSCredentialsConfigurator().configure(session.getHost(), prompt);
                                final Credentials credentials = session.getHost().getCredentials();
                                credentials.setUsername(auto.getUsername());
                                credentials.setPassword(auto.getPassword());
                                credentials.setToken(auto.getToken());
                                return true;
                            }
                            catch(LoginFailureException e) {
                                log.warn("Attempt to renew expired token failed");
                            }
                        }
                    }
                    catch(IOException e) {
                        log.warn(String.format("Failure parsing response entity from %s", response));
                    }
                }
        }
        return false;
    }
}
