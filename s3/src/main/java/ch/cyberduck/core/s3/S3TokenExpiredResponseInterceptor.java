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
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSSessionCredentials;

import java.io.IOException;

public class S3TokenExpiredResponseInterceptor extends DisabledServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(S3TokenExpiredResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final S3Session session;
    private final CredentialsConfigurator configurator;

    public S3TokenExpiredResponseInterceptor(final S3Session session, final CredentialsConfigurator configurator) {
        this.session = session;
        this.configurator = configurator;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        if(executionCount <= MAX_RETRIES) {
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
                                final Credentials credentials = configurator.configure(session.getHost());
                                if(log.isDebugEnabled()) {
                                    log.debug(String.format("Reconfigure client with credentials %s", credentials));
                                }
                                if(credentials.isTokenAuthentication()) {
                                    session.getClient().setProviderCredentials(new AWSSessionCredentials(
                                            credentials.getUsername(), credentials.getPassword(), credentials.getToken()));
                                }
                                else {
                                    session.getClient().setProviderCredentials(new AWSCredentials(
                                            credentials.getUsername(), credentials.getPassword()));
                                }
                                return true;
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

    public Credentials refresh() throws BackgroundException {
        return configurator.reload().configure(session.getHost());
    }
}
