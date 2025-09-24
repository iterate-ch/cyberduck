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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.ProxyPreferencesReader;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequestInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Swap static access key id and secret access key with temporary credentials obtained from STS AssumeRole
 */
public class STSAssumeRoleRequestInterceptor extends STSRequestInterceptor implements S3CredentialsStrategy, HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(STSAssumeRoleRequestInterceptor.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final Host host;

    public STSAssumeRoleRequestInterceptor(final Host host, final X509TrustManager trust, final X509KeyManager key, final LoginCallback prompt) {
        super(host, trust, key, prompt);
        this.host = host;
    }

    @Override
    public TemporaryAccessTokens refresh(final Credentials credentials) throws BackgroundException {
        lock.lock();
        try {
            if(StringUtils.isNotBlank(new ProxyPreferencesReader(host, credentials).getProperty(Profile.STS_ROLE_ARN_PROPERTY_KEY, "s3.assumerole.rolearn"))) {
                log.debug("Retrieve temporary credentials with {}", credentials);
                // AssumeRoleRequest
                return tokens = this.assumeRole(credentials);
            }
            log.warn("Skip requesting tokens from token service for {}", credentials);
            return TemporaryAccessTokens.EMPTY;
        }
        finally {
            lock.unlock();
        }
    }
}
