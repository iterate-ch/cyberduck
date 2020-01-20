package ch.cyberduck.core.oauth;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.local.BrowserLauncher;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.util.concurrent.Uninterruptibles;

public class OAuth2AuthorizationService {
    private static final Logger log = Logger.getLogger(OAuth2AuthorizationService.class);

    public static final String OOB_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    public static final String CYBERDUCK_REDIRECT_URI = String.format("%s:oauth", PreferencesFactory.get().getProperty("oauth.handler.scheme"));

    private final JsonFactory json
        = new JacksonFactory();

    private final String tokenServerUrl;
    private final String authorizationServerUrl;

    private final String clientid;
    private final String clientsecret;

    public final BrowserLauncher browser
        = BrowserLauncherFactory.get();

    private final List<String> scopes;

    private final Map<String, String> additionalParameters
        = new HashMap<>();

    private Credential.AccessMethod method
        = BearerToken.authorizationHeaderAccessMethod();

    private String redirectUri = OOB_REDIRECT_URI;

    private final HttpTransport transport;

    public OAuth2AuthorizationService(final HttpClient client,
                                      final String tokenServerUrl, final String authorizationServerUrl,
                                      final String clientid, final String clientsecret, final List<String> scopes) {
        this(new ApacheHttpTransport(client),
            tokenServerUrl, authorizationServerUrl, clientid, clientsecret, scopes);
    }

    public OAuth2AuthorizationService(final HttpTransport transport,
                                      final String tokenServerUrl, final String authorizationServerUrl,
                                      final String clientid, final String clientsecret, final List<String> scopes) {
        this.transport = transport;
        this.tokenServerUrl = tokenServerUrl;
        this.authorizationServerUrl = authorizationServerUrl;
        this.clientid = clientid;
        this.clientsecret = clientsecret;
        this.scopes = scopes;
    }

    public OAuthTokens authorize(final Host bookmark, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = bookmark.getCredentials();
        final OAuthTokens saved = credentials.getOauth();
        if(saved.validate()) {
            // Found existing tokens
            if(saved.isExpired()) {
                log.warn(String.format("Refresh expired access tokens %s", saved));
                // Refresh expired access key
                try {
                    return this.refresh(saved);
                }
                catch(LoginFailureException | InteroperabilityException e) {
                    log.warn(String.format("Failure refreshing tokens from %s for %s", saved, bookmark));
                    // Continue with new OAuth 2 flow
                }
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Returned saved OAuth tokens %s for %s", saved, bookmark));
                }
                return saved;
            }
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Start new OAuth flow for %s with missing access token", bookmark));
        }
        // Start OAuth2 flow within browser
        final AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
            method,
            transport, json,
            new GenericUrl(tokenServerUrl),
            new ClientParametersAuthentication(clientid, clientsecret),
            clientid,
            authorizationServerUrl)
            .setScopes(scopes)
            .build();
        final AuthorizationCodeRequestUrl authorizationCodeRequestUrl = flow.newAuthorizationUrl();
        authorizationCodeRequestUrl.setRedirectUri(redirectUri);
        final String state = new AlphanumericRandomStringService().random();
        authorizationCodeRequestUrl.setState(state);
        for(Map.Entry<String, String> values : additionalParameters.entrySet()) {
            authorizationCodeRequestUrl.set(values.getKey(), values.getValue());
        }
        // Direct the user to an authorization page to grant access to their protected data.
        final String url = authorizationCodeRequestUrl.build();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open browser with URL %s", url));
        }
        if(!browser.open(url)) {
            log.warn(String.format("Failed to launch web browser for %s", url));
        }
        final AtomicReference<String> authenticationCode = new AtomicReference<>();
        if(StringUtils.contains(redirectUri, CYBERDUCK_REDIRECT_URI)) {
            final CountDownLatch signal = new CountDownLatch(1);
            final OAuth2TokenListenerRegistry registry = OAuth2TokenListenerRegistry.get();
            registry.register(state, new OAuth2TokenListener() {
                @Override
                public void callback(final String code) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Callback with code %s", code));
                    }
                    if(!StringUtils.isBlank(code)) {
                        credentials.setSaved(PreferencesFactory.get().getBoolean("connection.login.keychain"));
                        authenticationCode.set(code);
                    }
                    signal.countDown();
                }
            });
            while(!Uninterruptibles.awaitUninterruptibly(signal, 500, TimeUnit.MILLISECONDS)) {
                cancel.verify();
            }
        }
        else {
            final Credentials input = prompt.prompt(bookmark,
                LocaleFactory.localizedString("OAuth2 Authentication", "Credentials"),
                LocaleFactory.localizedString("Paste the authentication code from your web browser", "Credentials"),
                new LoginOptions(bookmark.getProtocol()).keychain(true).user(false).oauth(true)
                    .passwordPlaceholder(LocaleFactory.localizedString("Authentication Code", "Credentials"))
            );
            credentials.setSaved(input.isSaved());
            authenticationCode.set(input.getPassword());
        }
        try {
            if(StringUtils.isBlank(authenticationCode.get())) {
                throw new LoginCanceledException();
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Request tokens for authentication code %s", authenticationCode.get()));
            }
            // Swap the given authorization token for access/refresh tokens
            final TokenResponse response = flow.newTokenRequest(authenticationCode.get())
                .setRedirectUri(redirectUri).setScopes(scopes.isEmpty() ? null : scopes)
                .executeUnparsed().parseAs(PermissiveTokenResponse.class).toTokenResponse();
            // Save access key and refresh key
            final OAuthTokens tokens = new OAuthTokens(
                response.getAccessToken(), response.getRefreshToken(),
                null == response.getExpiresInSeconds() ? System.currentTimeMillis() :
                    System.currentTimeMillis() + response.getExpiresInSeconds() * 1000);
            credentials.setOauth(tokens);
            return tokens;
        }
        catch(TokenResponseException e) {
            throw new OAuthExceptionMappingService().map(e);
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(new org.apache.http.client
                .HttpResponseException(e.getStatusCode(), e.getStatusMessage()));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public OAuthTokens refresh(final OAuthTokens tokens) throws BackgroundException {
        if(StringUtils.isBlank(tokens.getRefreshToken())) {
            log.warn("Missing refresh token");
            return tokens;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Refresh expired tokens %s", tokens));
        }
        try {
            final TokenResponse response = new RefreshTokenRequest(transport, json, new GenericUrl(tokenServerUrl),
                tokens.getRefreshToken())
                .setClientAuthentication(new ClientParametersAuthentication(clientid, clientsecret))
                .executeUnparsed().parseAs(PermissiveTokenResponse.class).toTokenResponse();
            final long expiryInMilliseconds = System.currentTimeMillis() + response.getExpiresInSeconds() * 1000;
            if(StringUtils.isBlank(response.getRefreshToken())) {
                return new OAuthTokens(response.getAccessToken(), tokens.getRefreshToken(), expiryInMilliseconds);
            }
            return new OAuthTokens(response.getAccessToken(), response.getRefreshToken(), expiryInMilliseconds);
        }
        catch(TokenResponseException e) {
            throw new OAuthExceptionMappingService().map(e);
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(new org.apache.http.client
                .HttpResponseException(e.getStatusCode(), e.getStatusMessage()));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public OAuth2AuthorizationService withMethod(final Credential.AccessMethod method) {
        this.method = method;
        return this;
    }

    public OAuth2AuthorizationService withRedirectUri(final String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public OAuth2AuthorizationService withParameter(final String key, final String value) {
        additionalParameters.put(key, value);
        return this;
    }

    public static final class PermissiveTokenResponse extends GenericJson {
        private String accessToken;
        private String tokenType;
        private Long expiresInSeconds;
        private String refreshToken;
        private String scope;

        @Override
        public PermissiveTokenResponse set(final String fieldName, final Object value) {
            if("access_token".equals(fieldName)) {
                accessToken = (String) value;
            }
            else if("refresh_token".equals(fieldName)) {
                refreshToken = (String) value;
            }
            else if("token_type".equals(fieldName)) {
                tokenType = (String) value;
            }
            else if("scope".equals(fieldName)) {
                scope = (String) value;
            }
            else if("expires_in".equals(fieldName)) {
                if(value instanceof String) {
                    try {
                        expiresInSeconds = Long.parseLong((String) value);
                    }
                    catch(NumberFormatException e) {
                        throw new IllegalArgumentException("Value of expires_in is not a number: " + value);
                    }
                }
                else if(value instanceof Number) {
                    expiresInSeconds = ((Number) value).longValue();
                }
                else {
                    throw new IllegalArgumentException("Unknown value type for expires_in: " + value.getClass().getName());
                }
            }
            else {
                return (PermissiveTokenResponse) super.set(fieldName, value);
            }
            return this;
        }

        public TokenResponse toTokenResponse() {
            return new TokenResponse()
                .setTokenType(tokenType)
                .setScope(scope)
                .setExpiresInSeconds(expiresInSeconds)
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);
        }
    }
}
