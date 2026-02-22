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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
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
                                             final X509TrustManager trust, final X509KeyManager key, final LoginCallback prompt) throws LoginCanceledException {
        super(host, trust, key);
        this.oauth = oauth;
        this.host = host;
        this.region = prompt(host, prompt, Profile.SSO_REGION_KEY, LocaleFactory.localizedString(
                "SSO Region", "Credentials"), host.getProperty(Profile.SSO_REGION_KEY));
        this.accountId = prompt(host, prompt, Profile.SSO_ACCOUNT_ID_KEY, LocaleFactory.localizedString(
                "AWS Account ID", "Credentials"), host.getProperty(Profile.SSO_ACCOUNT_ID_KEY));
        this.roleName = prompt(host, prompt, Profile.SSO_ROLE_NAME_KEY, LocaleFactory.localizedString(
                "Permission set name", "Credentials"), host.getProperty(Profile.SSO_ROLE_NAME_KEY));
    }

    public TemporaryAccessTokens refresh(final Credentials credentials) throws BackgroundException {
        final OAuthTokens accessKey = oauth.validate(credentials.getOauth());
        final RoleCredentials roleCredentials = this.getRoleCredentials(accessKey, region, accountId, roleName);
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

    /**
     * Prompt for value if missing
     */
    private static String prompt(final Host bookmark, final PasswordCallback prompt,
                                 final String property, final String message, final String value) throws LoginCanceledException {
        if(null == value) {
            final Credentials input = prompt.prompt(bookmark, message,
                    LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                    new LoginOptions().icon(bookmark.getProtocol().disk())
                            .passwordPlaceholder(message).password(false));
            if(input.isSaved()) {
                HostPreferencesFactory.get(bookmark).setProperty(property, input.getPassword());
            }
            return input.getPassword();
        }
        if(StringUtils.isBlank(value)) {
            return null;
        }
        return value;
    }
}
