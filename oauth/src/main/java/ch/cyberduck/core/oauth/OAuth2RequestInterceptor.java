package ch.cyberduck.core.oauth;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;

public class OAuth2RequestInterceptor extends OAuth2AuthorizationService implements HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(OAuth2RequestInterceptor.class);

    /**
     * Currently valid tokens
     */
    private OAuthTokens tokens = OAuthTokens.EMPTY;

    private final HostPasswordStore store = PasswordStoreFactory.get();
    private final Host host;

    public OAuth2RequestInterceptor(final HttpClient client, final Host host, final LoginCallback prompt) throws LoginCanceledException {
        this(client, host,
                Scheme.isURL(host.getProtocol().getOAuthTokenUrl()) ? host.getProtocol().getOAuthTokenUrl() : new HostUrlProvider().withUsername(false).withPath(true).get(
                        host.getProtocol().getScheme(), host.getPort(), null, host.getHostname(), host.getProtocol().getOAuthTokenUrl()),
                Scheme.isURL(host.getProtocol().getOAuthAuthorizationUrl()) ? host.getProtocol().getOAuthAuthorizationUrl() : new HostUrlProvider().withUsername(false).withPath(true).get(
                        host.getProtocol().getScheme(), host.getPort(), null, host.getHostname(), host.getProtocol().getOAuthAuthorizationUrl()),
                host.getProtocol().getOAuthClientId(),
                host.getProtocol().getOAuthClientSecret(),
                host.getProtocol().getOAuthScopes(),
                host.getProtocol().isOAuthPKCE(), prompt);
    }

    public OAuth2RequestInterceptor(final HttpClient client, final Host host, final String tokenServerUrl, final String authorizationServerUrl,
                                    final String clientid, final String clientsecret, final List<String> scopes, final boolean pkce, final LoginCallback prompt) throws LoginCanceledException {
        super(client, host, tokenServerUrl, authorizationServerUrl, clientid, clientsecret, scopes, pkce, prompt);
        this.host = host;
    }

    @Override
    public OAuthTokens authorize(final Host bookmark, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        return tokens = super.authorize(bookmark, prompt, cancel);
    }

    public OAuthTokens refresh() throws BackgroundException {
        return tokens = this.refresh(tokens);
    }

    @Override
    public OAuthTokens refresh(final OAuthTokens previous) throws BackgroundException {
        return tokens = super.refresh(previous);
    }

    /**
     * Save updated tokens in keychain
     *
     * @return Same tokens saved
     */
    public OAuthTokens save(final OAuthTokens tokens) throws LocalAccessDeniedException {
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
                final OAuthTokens previous = tokens;
                final OAuthTokens refreshed = this.refresh(tokens);
                // Skip saving tokens when not changed
                if(!refreshed.equals(previous)) {
                    this.save(refreshed);
                }
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure %s refreshing OAuth tokens %s", e, tokens));
                // Follow-up error 401 handled in error interceptor
            }
        }
        if(StringUtils.isNotBlank(tokens.getAccessToken())) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Authorizing service request with OAuth2 tokens %s", tokens));
            }
            request.removeHeaders(HttpHeaders.AUTHORIZATION);
            request.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", tokens.getAccessToken())));
        }
    }

    @Override
    public OAuth2RequestInterceptor withMethod(final Credential.AccessMethod method) {
        super.withMethod(method);
        return this;
    }

    @Override
    public OAuth2RequestInterceptor withRedirectUri(final String redirectUri) {
        super.withRedirectUri(redirectUri);
        return this;
    }

    @Override
    public OAuth2RequestInterceptor withFlowType(final FlowType flowType) {
        super.withFlowType(flowType);
        return this;
    }

    @Override
    public OAuth2RequestInterceptor withParameter(final String key, final String value) {
        super.withParameter(key, value);
        return this;
    }
}
