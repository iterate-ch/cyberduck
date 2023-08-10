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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.security.AWSSessionCredentials;

import java.io.IOException;

public class STSAssumeRoleCredentialsRequestInterceptor extends STSAssumeRoleAuthorizationService implements HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(STSAssumeRoleCredentialsRequestInterceptor.class);

    /**
     * Currently valid tokens
     */
    private STSTokens tokens = STSTokens.EMPTY;

    private final HostPasswordStore store = PasswordStoreFactory.get();
    private final OAuth2RequestInterceptor oauth;
    private final S3Session session;
    private final Host host;

    public STSAssumeRoleCredentialsRequestInterceptor(final OAuth2RequestInterceptor oauth, final S3Session session, final X509TrustManager trust, final X509KeyManager key) {
        super(session.getHost(), trust, key);
        this.oauth = oauth;
        this.session = session;
        this.host = session.getHost();
    }

    public STSTokens refresh() throws BackgroundException {
        return this.refresh(oauth.refresh());
    }

    public STSTokens refresh(final OAuthTokens oauth) throws BackgroundException {
        return this.tokens = this.authorize(host, oauth);
    }

    /**
     * Save updated tokens in keychain
     *
     * @return Same tokens saved
     */
    public STSTokens save(final STSTokens tokens) throws LocalAccessDeniedException {
        host.getCredentials()
                .withUsername(tokens.getAccessKeyId())
                .withPassword(tokens.getSecretAccessKey())
                .withToken(tokens.getSessionToken())
                .withSaved(new LoginOptions().keychain);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Save new tokens %s for %s", tokens, host));
        }
        store.save(host);
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
}
