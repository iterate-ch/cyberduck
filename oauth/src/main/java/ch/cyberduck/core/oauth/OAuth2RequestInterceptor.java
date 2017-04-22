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

import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;

public class OAuth2RequestInterceptor extends OAuth2AuthorizationService implements HttpRequestInterceptor {
    private static final Logger log = Logger.getLogger(OAuth2RequestInterceptor.class);

    /**
     * Currently valid tokens
     */
    private Tokens tokens = Tokens.EMPTY;

    public OAuth2RequestInterceptor(final HttpClient client, final String tokenServerUrl, final String authorizationServerUrl, final String clientid, final String clientsecret, final List<String> scopes) {
        super(client, tokenServerUrl, authorizationServerUrl, clientid, clientsecret, scopes);
    }

    public OAuth2RequestInterceptor(final HttpTransport transport, final String tokenServerUrl, final String authorizationServerUrl, final String clientid, final String clientsecret, final List<String> scopes) {
        super(transport, tokenServerUrl, authorizationServerUrl, clientid, clientsecret, scopes);
    }

    public void setTokens(final Tokens tokens) {
        this.tokens = tokens;
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        if(tokens.isExpired()) {
            try {
                tokens = this.refresh(tokens);
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure refreshing OAuth 2 tokens. %s", e.getDetail()));
                throw new IOException(e);
            }
        }
        if(StringUtils.isNotBlank(tokens.getAccessToken())) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Authorizing service request with OAuth2 access token %s", tokens.getAccessToken()));
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
    public OAuth2RequestInterceptor withParameter(final String key, final String value) {
        super.withParameter(key, value);
        return this;
    }
}
