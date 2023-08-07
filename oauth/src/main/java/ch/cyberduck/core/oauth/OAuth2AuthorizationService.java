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
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.UserAgentHttpRequestInitializer;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.PasswordTokenRequest;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

public class OAuth2AuthorizationService {
    private static final Logger log = LogManager.getLogger(OAuth2AuthorizationService.class);

    public static final String OOB_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    public static final String CYBERDUCK_REDIRECT_URI = String.format("%s:oauth", PreferencesFactory.get().getProperty("oauth.handler.scheme"));

    private final JsonFactory json
            = new GsonFactory();

    private final String tokenServerUrl;
    private final String authorizationServerUrl;

    private final String clientid;
    private final String clientsecret;

    private final List<String> scopes;
    private final boolean pkce;

    private final Map<String, String> additionalParameters
            = new HashMap<>();

    private Credential.AccessMethod method
            = BearerToken.authorizationHeaderAccessMethod();

    private String redirectUri = OOB_REDIRECT_URI;
    private FlowType flowType = FlowType.AuthorizationCode;

    private final HttpTransport transport;

    public OAuth2AuthorizationService(final HttpClient client,
                                      final String tokenServerUrl, final String authorizationServerUrl,
                                      final String clientid, final String clientsecret, final List<String> scopes, final boolean pkce) {
        this(new ApacheHttpTransport(client),
                tokenServerUrl, authorizationServerUrl, clientid, clientsecret, scopes, pkce);
    }

    public OAuth2AuthorizationService(final HttpTransport transport,
                                      final String tokenServerUrl, final String authorizationServerUrl,
                                      final String clientid, final String clientsecret, final List<String> scopes, final boolean pkce) {
        this.transport = transport;
        this.tokenServerUrl = tokenServerUrl;
        this.authorizationServerUrl = authorizationServerUrl;
        this.clientid = clientid;
        this.clientsecret = clientsecret;
        this.scopes = scopes;
        this.pkce = pkce;
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
                    return credentials.withOauth(this.refresh(saved)).withSaved(new LoginOptions().keychain).getOauth();
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

        final IdTokenResponse response;
        // Save access token, refresh token and id token
        switch(flowType) {
            case AuthorizationCode:
                response = this.authorizeWithCode(bookmark, prompt);
                return credentials.withOauth(new OAuthTokens(
                                response.getAccessToken(), response.getRefreshToken(),
                                null == response.getExpiresInSeconds() ? System.currentTimeMillis() :
                                        System.currentTimeMillis() + response.getExpiresInSeconds() * 1000, response.getIdToken()))
                        .withSaved(new LoginOptions().keychain).getOauth();
            case PasswordGrant:
                response = this.authorizeWithPassword(credentials);
                return credentials.withOauth(new OAuthTokens(
                                response.getAccessToken(), response.getRefreshToken(),
                                null == response.getExpiresInSeconds() ? System.currentTimeMillis() :
                                        System.currentTimeMillis() + response.getExpiresInSeconds() * 1000, response.getIdToken()))
                        .withSaved(new LoginOptions().keychain).getOauth();
            default:
                throw new LoginCanceledException();
        }
    }

    private IdTokenResponse authorizeWithCode(final Host bookmark, final LoginCallback prompt) throws BackgroundException {
        if(PreferencesFactory.get().getBoolean("oauth.browser.open.warn")) {
            prompt.warn(bookmark,
                    LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                    new StringAppender()
                            .append(LocaleFactory.localizedString("Open web browser to authenticate and obtain an authorization code", "Credentials"))
                            .append(LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support")).toString(),
                    LocaleFactory.localizedString("Continue", "Credentials"),
                    LocaleFactory.localizedString("Cancel"), "oauth.browser.open.warn"
            );
        }
        // Start OAuth2 flow within browser
        final AuthorizationCodeFlow.Builder flowBuilder = new AuthorizationCodeFlow.Builder(
                method,
                transport, json,
                new GenericUrl(tokenServerUrl),
                new ClientParametersAuthentication(clientid, clientsecret),
                clientid,
                authorizationServerUrl)
                .setScopes(scopes)
                .setRequestInitializer(new UserAgentHttpRequestInitializer(new PreferencesUseragentProvider()));
        if(pkce) {
            flowBuilder.enablePKCE();
        }
        final AuthorizationCodeFlow flow = flowBuilder.build();
        final AuthorizationCodeRequestUrl authorizationCodeUrlBuilder = flow.newAuthorizationUrl();
        authorizationCodeUrlBuilder.setRedirectUri(redirectUri);
        final String state = new AlphanumericRandomStringService().random();
        authorizationCodeUrlBuilder.setState(state);
        for(Map.Entry<String, String> values : additionalParameters.entrySet()) {
            authorizationCodeUrlBuilder.set(values.getKey(), values.getValue());
        }
        // Direct the user to an authorization page to grant access to their protected data.
        final String authorizationCodeUrl = authorizationCodeUrlBuilder.build();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open browser with URL %s", authorizationCodeUrl));
        }
        final String authorizationCode = OAuth2AuthorizationCodeProviderFactory.get().prompt(
                bookmark, prompt, authorizationCodeUrl, redirectUri, state);
        if(StringUtils.isBlank(authorizationCode)) {
            throw new LoginCanceledException();
        }
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Request tokens for authentication code %s", authorizationCode));
            }
            // Swap the given authorization token for access/refresh tokens
            return flow.newTokenRequest(authorizationCode)
                    .setRedirectUri(redirectUri).setScopes(scopes.isEmpty() ? null : scopes)
                    .executeUnparsed().parseAs(PermissiveTokenResponse.class).toTokenResponse();
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

    private IdTokenResponse authorizeWithPassword(final Credentials credentials) throws BackgroundException {
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Request tokens for user %s", credentials.getUsername()));
            }
            final PasswordTokenRequest request = new PasswordTokenRequest(transport, json, new GenericUrl(tokenServerUrl),
                    credentials.getUsername(), credentials.getPassword()
            )
                    .setClientAuthentication(new ClientParametersAuthentication(clientid, clientsecret))
                    .setRequestInitializer(new UserAgentHttpRequestInitializer(new PreferencesUseragentProvider()))
                    .setScopes(scopes.isEmpty() ? null : scopes);
            for(Map.Entry<String, String> values : additionalParameters.entrySet()) {
                request.set(values.getKey(), values.getValue());
            }
            return request.executeUnparsed().parseAs(PermissiveTokenResponse.class).toTokenResponse();
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
            final IdTokenResponse response = new RefreshTokenRequest(transport, json, new GenericUrl(tokenServerUrl),
                    tokens.getRefreshToken())
                    .setScopes(scopes.isEmpty() ? null : scopes)
                    .setRequestInitializer(new UserAgentHttpRequestInitializer(new PreferencesUseragentProvider()))
                    .setClientAuthentication(new ClientParametersAuthentication(clientid, clientsecret))
                    .executeUnparsed().parseAs(PermissiveTokenResponse.class).toTokenResponse();
            final long expiryInMilliseconds = System.currentTimeMillis() + response.getExpiresInSeconds() * 1000;
            if(StringUtils.isBlank(response.getRefreshToken())) {
                return new OAuthTokens(response.getAccessToken(), tokens.getRefreshToken(), expiryInMilliseconds, response.getIdToken());
            }
            return new OAuthTokens(response.getAccessToken(), response.getRefreshToken(), expiryInMilliseconds, response.getIdToken());
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

    public OAuth2AuthorizationService withFlowType(final FlowType flowType) {
        this.flowType = flowType;
        return this;
    }

    public OAuth2AuthorizationService withParameter(final String key, final String value) {
        additionalParameters.put(key, value);
        return this;
    }

    public enum FlowType {
        AuthorizationCode,
        PasswordGrant
    }

    public static final class PermissiveTokenResponse extends GenericJson {
        private String idToken;
        private String accessToken;
        private String tokenType;
        private Long expiresInSeconds;
        private String refreshToken;
        private String scope;

        @Override
        public PermissiveTokenResponse set(final String fieldName, final Object value) {
            if("id_token".equals(fieldName)) {
                idToken = (String) value;
            }
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

        public IdTokenResponse toTokenResponse() {
            IdTokenResponse tokenResponse = new IdTokenResponse()
                    .setTokenType(tokenType)
                    .setScope(scope)
                    .setExpiresInSeconds(expiresInSeconds)
                    .setRefreshToken(refreshToken);

            if(StringUtils.isNotBlank(accessToken)) {
                tokenResponse.setAccessToken(accessToken);
            }
            if(StringUtils.isNotBlank(idToken)) {
                tokenResponse.setIdToken(idToken);
            }
            return tokenResponse;
        }
    }

}
