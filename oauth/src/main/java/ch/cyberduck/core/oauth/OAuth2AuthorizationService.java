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
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.UserAgentHttpRequestInitializer;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
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

    private final Host host;
    /**
     * Static long-lived credentials
     */
    private final Credentials credentials;
    private final LoginCallback prompt;

    private String clientid;
    private String clientsecret;
    private String tokenServerUrl;
    private String authorizationServerUrl;
    private String redirectUri = OOB_REDIRECT_URI;
    private FlowType flowType = FlowType.AuthorizationCode;

    private final List<String> scopes;
    private final boolean pkce;

    private final Map<String, String> additionalParameters
            = new HashMap<>();

    private Credential.AccessMethod method
            = BearerToken.authorizationHeaderAccessMethod();

    private final HttpTransport transport;

    private final HostPasswordStore store = PasswordStoreFactory.get();

    public OAuth2AuthorizationService(final HttpClient client, final Host host,
                                      final String tokenServerUrl, final String authorizationServerUrl,
                                      final String clientid, final String clientsecret, final List<String> scopes, final boolean pkce,
                                      final LoginCallback prompt) {
        this(new ApacheHttpTransport(client), host,
                tokenServerUrl, authorizationServerUrl, clientid, clientsecret, scopes, pkce, prompt);
    }

    public OAuth2AuthorizationService(final HttpTransport transport, final Host host,
                                      final String tokenServerUrl, final String authorizationServerUrl,
                                      final String clientid, final String clientsecret, final List<String> scopes, final boolean pkce,
                                      final LoginCallback prompt) {
        this.transport = transport;
        this.host = host;
        this.credentials = host.getCredentials();
        this.tokenServerUrl = tokenServerUrl;
        this.authorizationServerUrl = authorizationServerUrl;
        this.prompt = prompt;
        this.clientid = clientid;
        this.clientsecret = clientsecret;
        this.scopes = scopes;
        this.pkce = pkce;
    }

    /**
     * Authorize when cached tokens expired otherwise return
     *
     * @return Tokens retrieved
     */
    public OAuthTokens validate(final OAuthTokens saved) throws BackgroundException {
        if(saved.validate()) {
            // Found existing tokens
            if(saved.isExpired()) {
                log.warn("Refresh expired tokens {}", saved);
                // Refresh expired tokens
                try {
                    final OAuthTokens refreshed = this.authorizeWithRefreshToken(saved);
                    log.debug("Refreshed tokens {} for {}", refreshed, host);
                    return this.save(refreshed);
                }
                catch(LoginFailureException e) {
                    log.warn("Failure refreshing tokens from {} for {}", saved, host);
                    // Continue with authorization flow
                }
            }
            else {
                log.debug("Returned saved tokens {} for {}", saved, host);
                return saved;
            }
        }
        log.warn("Missing tokens {} for {}", saved, host);
        final OAuthTokens tokens = this.authorize();
        log.debug("Retrieved tokens {} for {}", tokens, host);
        return tokens;
    }

    /**
     * Save updated tokens in keychain
     *
     * @return Same tokens saved
     */
    public OAuthTokens save(final OAuthTokens tokens) throws AccessDeniedException {
        log.debug("Save new tokens {} for {}", tokens, host);
        credentials.setOauth(tokens).setSaved(new LoginOptions().save);
        switch(flowType) {
            case PasswordGrant:
                // Skip modifying username used for password grant
                break;
            default:
                try {
                    final DecodedJWT jwt = JWT.decode(tokens.getIdToken());
                    // Standard claims
                    for(String claim : new String[]{"preferred_username", "email", "name", "nickname", "sub"}) {
                        final String value = jwt.getClaim(claim).asString();
                        if(StringUtils.isNotBlank(value)) {
                            log.debug("Set username to {} from claim {}", value, claim);
                            credentials.setUsername(value);
                            break;
                        }
                    }
                }
                catch(JWTDecodeException e) {
                    log.warn("Failure {} decoding JWT {}", e, tokens.getIdToken());
                }
                break;
        }
        if(credentials.isSaved()) {
            store.save(host);
        }
        return tokens;
    }


    /**
     * @return Tokens retrieved
     */
    public OAuthTokens authorize() throws BackgroundException {
        log.debug("Start new OAuth flow for {} with missing access token", host);
        final IdTokenResponse response;
        // Save access token, refresh token and id token
        switch(flowType) {
            case AuthorizationCode:
                response = this.authorizeWithCode(prompt);
                break;
            case PasswordGrant:
                response = this.authorizeWithPassword(credentials);
                break;
            default:
                throw new LoginCanceledException();
        }
        return new OAuthTokens(
                response.getAccessToken(), response.getRefreshToken(),
                null == response.getExpiresInSeconds() ? Long.MAX_VALUE :
                        System.currentTimeMillis() + response.getExpiresInSeconds() * 1000, response.getIdToken());
    }

    private IdTokenResponse authorizeWithCode(final LoginCallback prompt) throws BackgroundException {
        log.debug("Request tokens with code");
        if(HostPreferencesFactory.get(host).getBoolean("oauth.browser.open.warn")) {
            prompt.warn(host,
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
                new ClientParametersAuthentication(clientid, StringUtils.isNotBlank(clientsecret) ? clientsecret : null),
                clientid,
                authorizationServerUrl)
                .setScopes(scopes)
                .setRequestInitializer(new UserAgentHttpRequestInitializer(new PreferencesUseragentProvider()));
        if(pkce) {
            flowBuilder.enablePKCE();
        }
        final AuthorizationCodeFlow flow = flowBuilder.build();
        final AuthorizationCodeRequestUrl authorizationCodeUrlBuilder = flow.newAuthorizationUrl();
        authorizationCodeUrlBuilder.setRedirectUri(URIEncoder.decode(redirectUri));
        final String state = new AlphanumericRandomStringService().random();
        authorizationCodeUrlBuilder.setState(state);
        for(Map.Entry<String, String> values : additionalParameters.entrySet()) {
            authorizationCodeUrlBuilder.set(values.getKey(), values.getValue());
        }
        // Direct the user to an authorization page to grant access to their protected data.
        final String authorizationCodeUrl = authorizationCodeUrlBuilder.build();
        log.debug("Open browser with URL {}", authorizationCodeUrl);
        final String authorizationCode = OAuth2AuthorizationCodeProviderFactory.get().prompt(
                host, prompt, authorizationCodeUrl, redirectUri, state);
        if(StringUtils.isBlank(authorizationCode)) {
            throw new LoginCanceledException();
        }
        return this.exchangeToken(flow, authorizationCode);
    }

    /**
     * Exchanges authorization code for access and refresh tokens
     */
    protected IdTokenResponse exchangeToken(final AuthorizationCodeFlow flow, final String authorizationCode) throws BackgroundException {
        try {
            log.debug("Request tokens for authentication code {}", authorizationCode);
            // Swap the given authorization token for access/refresh tokens
            return flow.newTokenRequest(authorizationCode)
                    .setRedirectUri(URIEncoder.decode(redirectUri)).setScopes(scopes.isEmpty() ? null : scopes)
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
            log.debug("Request tokens with password {}", credentials);
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

    public OAuthTokens authorizeWithRefreshToken(final OAuthTokens tokens) throws BackgroundException {
        if(StringUtils.isBlank(tokens.getRefreshToken())) {
            log.warn("Missing refresh token in {}", tokens);
            return this.authorize();
        }
        log.debug("Refresh expired tokens {}", tokens);
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

    public OAuth2AuthorizationService setClientid(final String clientid) {
        this.clientid = clientid;
        return this;
    }

    public String getClientid() {
        return clientid;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public OAuth2AuthorizationService setClientsecret(final String clientsecret) {
        this.clientsecret = clientsecret;
        return this;
    }

    public String getClientsecret() {
        return clientsecret;
    }

    public OAuth2AuthorizationService setTokenServerUrl(final String tokenServerUrl) {
        this.tokenServerUrl = tokenServerUrl;
        return this;
    }

    public OAuth2AuthorizationService setAuthorizationServerUrl(final String authorizationServerUrl) {
        this.authorizationServerUrl = authorizationServerUrl;
        return this;
    }

    public OAuth2AuthorizationService setMethod(final Credential.AccessMethod method) {
        this.method = method;
        return this;
    }

    public OAuth2AuthorizationService setRedirectUri(final String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public OAuth2AuthorizationService setFlowType(final FlowType flowType) {
        this.flowType = flowType;
        return this;
    }

    public FlowType getFlowType() {
        return flowType;
    }

    public OAuth2AuthorizationService setParameter(final String key, final String value) {
        additionalParameters.put(key, value);
        return this;
    }

    public Map<String, String> getAdditionalParameters() {
        return additionalParameters;
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
            final IdTokenResponse response = new IdTokenResponse()
                    .setTokenType(tokenType)
                    .setScope(scope)
                    .setExpiresInSeconds(expiresInSeconds)
                    .setAccessToken(accessToken)
                    .setRefreshToken(refreshToken);
            if(null == idToken) {
                return response;
            }
            return response.setIdToken(idToken);
        }
    }

    /**
     * Prompt for value if missing
     */
    protected static String prompt(final Host bookmark, final PasswordCallback prompt,
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
