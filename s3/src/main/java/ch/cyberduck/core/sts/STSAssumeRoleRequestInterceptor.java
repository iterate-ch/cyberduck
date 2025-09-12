package ch.cyberduck.core.sts;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.security.AWSSessionCredentials;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Swap static access key id and secret access key with temporary credentials obtained from STS AssumeRole
 */
public class STSAssumeRoleRequestInterceptor extends STSAssumeRoleAuthorizationService implements S3CredentialsStrategy, HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(STSAssumeRoleRequestInterceptor.class);

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Currently valid tokens
     */
    private TemporaryAccessTokens tokens = TemporaryAccessTokens.EMPTY;

    private final S3Session session;
    private final TemporaryAccessTokens credentials;

    public STSAssumeRoleRequestInterceptor(final S3Session session,
                                           final X509TrustManager trust, final X509KeyManager key,
                                           final LoginCallback prompt) {
        super(session.getHost(), trust, key, prompt);
        this.session = session;
        // Keep copy of credentials
        this.credentials = new TemporaryAccessTokens(session.getHost().getCredentials().getUsername(),
                session.getHost().getCredentials().getPassword(), session.getHost().getCredentials().getToken(), -1L);
    }

    public TemporaryAccessTokens refresh(final TemporaryAccessTokens credentials) throws BackgroundException {
        lock.lock();
        try {
            return this.tokens = this.authorize(credentials);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        lock.lock();
        try {
            if(tokens.isExpired()) {
                try {
                    this.refresh(credentials);
                    log.info("Authorizing service request with STS tokens {}", tokens);
                }
                catch(BackgroundException e) {
                    log.warn("Failure {} refreshing STS tokens {}", e, tokens);
                    // Follow-up error 401 handled in error interceptor
                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public Credentials get() throws BackgroundException {
        // Get temporary credentials from STS using static long-lived credentials
        return new Credentials().withTokens(this.refresh(credentials));
    }
}
