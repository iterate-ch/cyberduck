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
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
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

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;

/**
 * Swap OIDC Id token for temporary security credentials
 */
public class STSAssumeRoleCredentialsRequestInterceptor extends STSAssumeRoleAuthorizationService implements S3CredentialsStrategy, HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(STSAssumeRoleCredentialsRequestInterceptor.class);

    /**
     * Currently valid tokens
     */
    private TemporaryAccessTokens tokens = TemporaryAccessTokens.EMPTY;

    private final HostPasswordStore store = PasswordStoreFactory.get();
    /**
     * Handle authentication with OpenID connect retrieving token for STS
     */
    private final OAuth2RequestInterceptor oauth;
    private final S3Session session;

    public STSAssumeRoleCredentialsRequestInterceptor(final OAuth2RequestInterceptor oauth, final S3Session session,
                                                      final X509TrustManager trust, final X509KeyManager key,
                                                      final LoginCallback prompt) {
        super(session.getHost(), trust, key, prompt);
        this.oauth = oauth;
        this.session = session;
    }

    public STSAssumeRoleCredentialsRequestInterceptor(final OAuth2RequestInterceptor oauth, final S3Session session,
                                                      final AWSSecurityTokenService service, final LoginCallback prompt) {
        super(session.getHost(), service, prompt);
        this.oauth = oauth;
        this.session = session;
    }

    public TemporaryAccessTokens refresh(final OAuthTokens oidc) throws BackgroundException {
        try {
            return this.tokens = this.authorize(oidc);
        }
        catch(LoginFailureException e) {
            // Expired STS tokens
            log.warn("Failure {} authorizing. Retry with refreshed OAuth tokens", e.getMessage());
            return this.tokens = this.authorize(oauth.refresh(oidc));
        }
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        if(tokens.isExpired()) {
            try {
                this.refresh(oauth.getTokens());
                log.info("Authorizing service request with STS tokens {}", tokens);
                session.getClient().setProviderCredentials(new AWSSessionCredentials(tokens.getAccessKeyId(), tokens.getSecretAccessKey(),
                        tokens.getSessionToken()));
            }
            catch(BackgroundException e) {
                log.warn("Failure {} refreshing STS tokens {}", e, tokens);
                // Follow-up error 401 handled in error interceptor
            }
        }
    }

    @Override
    public Credentials get() throws BackgroundException {
        // Get temporary credentials from STS using Web Identity (OIDC) token
        final Credentials credentials = oauth.validate();
        final OAuthTokens identity = credentials.getOauth();
        final String token = this.getWebIdentityToken(identity);
        final String sub;
        try {
            sub = JWT.decode(token).getSubject();
        }
        catch(JWTDecodeException e) {
            throw new LoginFailureException("Invalid JWT or JSON format in authentication token", e);
        }
        final TemporaryAccessTokens tokens = this.refresh(identity);
        return credentials
                .withUsername(sub)
                .withTokens(tokens);
    }
}
