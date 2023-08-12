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
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.STSTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.security.AWSSessionCredentials;

import java.io.IOException;

/**
 * Swap OIDC Id token for temporary security credentials
 */
public class STSAssumeRoleCredentialsRequestInterceptor extends STSAssumeRoleAuthorizationService implements S3CredentialsStrategy, HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(STSAssumeRoleCredentialsRequestInterceptor.class);

    /**
     * Currently valid tokens
     */
    private STSTokens tokens = STSTokens.EMPTY;

    private final HostPasswordStore store = PasswordStoreFactory.get();
    /**
     * Handle authentication with OpenID connect retrieving token for STS
     */
    private final OAuth2RequestInterceptor oauth;
    private final S3Session session;
    private final Host host;
    private final LoginCallback prompt;
    private final CancelCallback cancel;

    public STSAssumeRoleCredentialsRequestInterceptor(final OAuth2RequestInterceptor oauth, final S3Session session,
                                                      final X509TrustManager trust, final X509KeyManager key,
                                                      final LoginCallback prompt, final CancelCallback cancel) {
        super(session.getHost(), trust, key, prompt);
        this.oauth = oauth;
        this.session = session;
        this.host = session.getHost();
        this.prompt = prompt;
        this.cancel = cancel;
    }

    public STSTokens refresh() throws BackgroundException {
        return this.tokens = this.authorize(host, oauth.refresh());
    }

    public STSTokens refresh(final OAuthTokens oauthTokens) throws BackgroundException {
        try {
            return this.tokens = this.authorize(host, oauthTokens);
        }
        catch(ExpiredTokenException e) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Failure %s authorizing. Retry with refreshed OAuth tokens", e));
            }
            return this.tokens = this.authorize(host, oauth.refresh());
        }
    }

    public STSTokens save(final STSTokens tokens) throws LocalAccessDeniedException {
        return tokens;
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        if(tokens.isExpired()) {
            try {
                this.save(this.refresh(oauth.getTokens()));
                if(log.isInfoEnabled()) {
                    log.info(String.format("Authorizing service request with STS tokens %s", tokens));
                }
                session.getClient().setProviderCredentials(new AWSSessionCredentials(tokens.getAccessKeyId(), tokens.getSecretAccessKey(),
                        tokens.getSessionToken()));
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure %s refreshing STS tokens %s", e, tokens));
                // Follow-up error 401 handled in error interceptor
            }
        }
    }

    @Override
    public Credentials get() throws BackgroundException {
        // Get temporary credentials from STS using Web Identity (OIDC) token
        return host.getCredentials().withTokens(this.authorize(host, oauth.authorize(host, prompt, cancel)));
    }
}
