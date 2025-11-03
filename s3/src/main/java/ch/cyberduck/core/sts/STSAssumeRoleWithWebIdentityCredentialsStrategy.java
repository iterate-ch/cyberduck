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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.ProxyPreferencesReader;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Swap OIDC Id token for temporary security credentials
 */
public class STSAssumeRoleWithWebIdentityCredentialsStrategy extends STSCredentialsStrategy implements S3CredentialsStrategy {
    private static final Logger log = LogManager.getLogger(STSAssumeRoleWithWebIdentityCredentialsStrategy.class);

    /**
     * Handle authentication with OpenID connect retrieving token for STS
     */
    private final OAuth2RequestInterceptor oauth;
    private final Host host;

    public STSAssumeRoleWithWebIdentityCredentialsStrategy(final OAuth2RequestInterceptor oauth, final Host host,
                                                           final X509TrustManager trust, final X509KeyManager key,
                                                           final LoginCallback prompt) {
        super(host, trust, key, prompt);
        this.oauth = oauth;
        this.host = host;
    }

    @Override
    public TemporaryAccessTokens refresh(final Credentials credentials) throws BackgroundException {
        final String arn = new ProxyPreferencesReader(host, credentials).getProperty(Profile.STS_ROLE_ARN_PROPERTY_KEY, "s3.assumerole.rolearn");
        log.debug("Retrieve temporary credentials with {} for role ARN {}", credentials, arn);
        return this.assumeRoleWithWebIdentity(oauth.validate(credentials.getOauth()), arn);
    }
}