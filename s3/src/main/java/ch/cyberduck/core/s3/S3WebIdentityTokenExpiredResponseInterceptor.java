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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DisabledServiceUnavailableRetryStrategy;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.sts.AssumeRoleWithWebIdentitySTSCredentialsConfigurator;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.S3ServiceException;
import com.google.api.client.http.HttpStatusCodes;
import org.jets3t.service.security.AWSSessionCredentials;

import java.io.IOException;

public class S3WebIdentityTokenExpiredResponseInterceptor extends DisabledServiceUnavailableRetryStrategy {
    private static final Logger log = LogManager.getLogger(S3WebIdentityTokenExpiredResponseInterceptor.class);

    private static final int MAX_RETRIES = 1;

    private final S3Session session;
    private final Host host;
    private final AssumeRoleWithWebIdentitySTSCredentialsConfigurator configurator;
    private final OAuth2RequestInterceptor authorizationService;

    private final LoginCallback prompt;

    public S3WebIdentityTokenExpiredResponseInterceptor(final S3Session session, final X509TrustManager trust,
                                                        final X509KeyManager key, final LoginCallback prompt,
                                                        OAuth2RequestInterceptor authorizationService) {
        this.session = session;
        this.host = session.getHost();
        this.configurator = new AssumeRoleWithWebIdentitySTSCredentialsConfigurator(trust, key, prompt);
        this.authorizationService = authorizationService;
        this.prompt = prompt;
    }

    @Override
    public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
        if(executionCount <= MAX_RETRIES) {
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_FORBIDDEN:
                    try {
                        if (host.getCredentials().getOauth().isExpired()) {
                            try {
                                host.getCredentials().setOauth(authorizationService.refresh());
                            }
                            catch(InteroperabilityException | LoginFailureException e3) {
                                log.warn(String.format("Failure %s refreshing OAuth tokens", e3));
                                authorizationService.authorize(host, prompt, new DisabledCancelCallback());
                            }
                        }

                        log.debug("OAuth refreshed. Refreshing STS token.");
                        System.out.println("Username:" + host.getCredentials().getUsername());
                        System.out.println("SessionToken:" + host.getCredentials().getToken());
                        System.out.println("Acc Key:" + session.getClient().getProviderCredentials().getAccessKey());
                        Credentials credentials = configurator.configure(host);
                        System.out.println("SessionToken MinIO: " + credentials.getToken());
                        session.getClient().setProviderCredentials(credentials.isAnonymousLogin() ? null :
                                new AWSSessionCredentials(credentials.getUsername(), credentials.getPassword(),
                                        credentials.getToken()));
                        System.out.println("SessionToken:" + host.getCredentials().getToken());
                        System.out.println("Acc Key:" + session.getClient().getProviderCredentials().getAccessKey());
                        System.out.println(session.getClient().getProviderCredentials().getSecretKey());

                        session.testafterrefresh();
                        return false;
                    }
                    catch(BackgroundException e) {
                        log.error("Failed to refresh OAuth in order to get STS", e);
                        throw new RuntimeException(e);
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