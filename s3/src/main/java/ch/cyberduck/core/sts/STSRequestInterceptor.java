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

import ch.cyberduck.core.CopyCredentialsHolder;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Swap static access key id and secret access key with temporary credentials obtained from STS AssumeRole
 */
public abstract class STSRequestInterceptor extends STSAuthorizationService implements S3CredentialsStrategy, HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(STSRequestInterceptor.class);

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Currently valid tokens obtained from token service
     */
    protected TemporaryAccessTokens tokens = TemporaryAccessTokens.EMPTY;

    /**
     * Static long-lived credentials
     */
    protected final Credentials credentials;

    public STSRequestInterceptor(final Host host, final X509TrustManager trust, final X509KeyManager key, final LoginCallback prompt) {
        super(host, trust, key, prompt);
        this.credentials = new CopyCredentialsHolder(host.getCredentials());
    }

    /**
     * Request new temporary access tokens from static access key in credentials
     *
     * @param credentials Static long-lived credentials
     * @return Temporary access tokens from STS service
     */
    public abstract TemporaryAccessTokens refresh(final Credentials credentials) throws BackgroundException;

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
        return credentials.setTokens(tokens.isExpired() ? this.refresh(credentials) : tokens);
    }
}
