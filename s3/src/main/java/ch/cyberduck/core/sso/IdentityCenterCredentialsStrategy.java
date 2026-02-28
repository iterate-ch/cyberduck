package ch.cyberduck.core.sso;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LocationCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.ReentrantLock;

import com.amazonaws.services.sso.model.RoleCredentials;

public class IdentityCenterCredentialsStrategy extends IdentityCenterAuthorizationService implements S3CredentialsStrategy {
    private static final Logger log = LogManager.getLogger(IdentityCenterCredentialsStrategy.class);

    private final ReentrantLock lock = new ReentrantLock();
    /**
     * Handle authentication with OpenID connect retrieving token for STS
     */
    private final OAuth2RequestInterceptor oauth;
    private final Host host;

    private final String region;
    private final String accountId;
    private final String roleName;

    public IdentityCenterCredentialsStrategy(final OAuth2RequestInterceptor oauth, final Host host,
                                             final X509TrustManager trust, final X509KeyManager key, final LoginCallback prompt) throws ConnectionCanceledException {
        super(host, trust, key, prompt);
        this.oauth = oauth;
        this.host = host;
        this.region = prompt(host, prompt.getFeature(LocationCallback.class), host.getProtocol().getRegions(), Profile.SSO_REGION_KEY,
                LocaleFactory.localizedString(String.format("SSO Region (%s)", Profile.SSO_REGION_KEY), "Credentials"),
                host.getProperty(Profile.SSO_REGION_KEY)).getIdentifier();
        this.accountId = host.getProperty(Profile.SSO_ACCOUNT_ID_KEY);
        this.roleName = host.getProperty(Profile.SSO_ROLE_NAME_KEY);
    }

    public TemporaryAccessTokens refresh(final Credentials credentials) throws BackgroundException {
        final RoleCredentials roleCredentials = this.getRoleCredentials(
                oauth.save(oauth.validate(credentials.getOauth())), region, accountId, roleName);
        log.debug("Received temporary access tokens {}", roleCredentials);
        return new TemporaryAccessTokens(roleCredentials.getAccessKeyId(),
                roleCredentials.getSecretAccessKey(), roleCredentials.getSessionToken(), roleCredentials.getExpiration());
    }

    @Override
    public Credentials get() throws BackgroundException {
        lock.lock();
        try {
            final Credentials credentials = host.getCredentials();
            final TemporaryAccessTokens tokens = credentials.getTokens();
            // Get temporary credentials from Identity Center
            if(tokens.isExpired()) {
                log.debug("Refresh expired tokens {} for {}", tokens, host);
                credentials.setTokens(this.refresh(credentials));
            }
            return credentials;
        }
        finally {
            lock.unlock();
        }
    }
}
