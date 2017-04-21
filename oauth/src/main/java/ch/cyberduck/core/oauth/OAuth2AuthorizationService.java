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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;
import ch.cyberduck.core.local.BrowserLauncher;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

public class OAuth2AuthorizationService {
    private static final Logger log = Logger.getLogger(OAuth2AuthorizationService.class);

    private static final String OOB_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    private static final String CYBERDUCK_REDIRECT_URI = "x-cyberduck-action:oauth";

    private final Preferences preferences
            = PreferencesFactory.get();

    private final JsonFactory json
            = new GsonFactory();

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

    public Tokens authorize(final Host bookmark, final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Tokens saved = this.find(keychain, bookmark);
        if(saved.validate()) {
            // Found existing tokens
            if(saved.isExpired()) {
                // Refresh expired access key
                return this.refresh(saved);
            }
            return saved;
        }
        else {
            // Obtain new tokens
            final Credentials input = new TokenCredentials(bookmark);
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
            for(Map.Entry<String, String> values : additionalParameters.entrySet()) {
                authorizationCodeRequestUrl.set(values.getKey(), values.getValue());
            }

            // Direct the user to an authorization page to grant access to their protected data.
            final String url = authorizationCodeRequestUrl.build();
            if(!browser.open(url)) {
                log.warn(String.format("Failed to launch web browser for %s", url));
            }
            if(StringUtils.equals(CYBERDUCK_REDIRECT_URI, redirectUri)) {
                final OAuth2TokenListenerRegistry registry = OAuth2TokenListenerRegistry.get();
                registry.register(new OAuth2TokenListener() {
                    @Override
                    public void callback(final String param) {
                        input.setPassword(param);
                    }
                }, cancel);
            }
            else {
                prompt.prompt(bookmark, input,
                        LocaleFactory.localizedString("OAuth2 Authentication", "Credentials"),
                        LocaleFactory.localizedString("Paste the authentication code from your web browser", "Credentials"),
                        new LoginOptions().keychain(true).user(false).password(true)
                );
            }
            try {
                if(StringUtils.isBlank(input.getPassword())) {
                    throw new LoginCanceledException();
                }
                // Swap the given authorization token for access/refresh tokens
                final TokenResponse response = flow.newTokenRequest(input.getPassword())
                        .setRedirectUri(redirectUri).setScopes(scopes.isEmpty() ? null : scopes).execute();
                // Save access key and refresh key
                final Tokens tokens = new Tokens(
                        response.getAccessToken(), response.getRefreshToken(),
                        System.currentTimeMillis() + response.getExpiresInSeconds() * 1000);
                if(input.isSaved()) {
                    this.save(keychain, bookmark, tokens);
                }
                return tokens;
            }
            catch(TokenResponseException e) {
                throw new OAuthExceptionMappingService().map(e);
            }
            catch(HttpResponseException e) {
                throw new HttpResponseExceptionMappingService().map(new org.apache.http.client
                        .HttpResponseException(e.getStatusCode(), e.getStatusMessage()));
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
    }

    public Tokens refresh(final Tokens tokens) throws BackgroundException {
        if(StringUtils.isBlank(tokens.getRefreshToken())) {
            log.warn("Missing refresh token");
            return tokens;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Refresh expired tokens %s", tokens));
        }
        try {
            final TokenResponse response = new RefreshTokenRequest(transport, json, new GenericUrl(tokenServerUrl),
                    tokens.getRefreshToken()).setClientAuthentication(new ClientParametersAuthentication(clientid, clientsecret)).execute();
            return new Tokens(response.getAccessToken(), response.getRefreshToken(), System.currentTimeMillis() + response.getExpiresInSeconds() * 1000);
        }
        catch(TokenResponseException e) {
            throw new OAuthExceptionMappingService().map(e);
        }
        catch(HttpResponseException e) {
            throw new HttpResponseExceptionMappingService().map(new org.apache.http.client
                    .HttpResponseException(e.getStatusCode(), e.getStatusMessage()));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public Tokens find(final HostPasswordStore keychain, final Host bookmark) {
        final long expiry = preferences.getLong(String.format("%s.oauth.expiry", bookmark.getProtocol().getIdentifier()));
        final String prefix = this.getPrefix(bookmark);
        return new Tokens(keychain.getPassword(bookmark.getProtocol().getScheme(),
                bookmark.getPort(), URI.create(tokenServerUrl).getHost(),
                String.format("%s OAuth2 Access Token", prefix)),
                keychain.getPassword(bookmark.getProtocol().getScheme(),
                        bookmark.getPort(), URI.create(tokenServerUrl).getHost(),
                        String.format("%s OAuth2 Refresh Token", prefix)),
                expiry);
    }

    private void save(final HostPasswordStore keychain, final Host bookmark, final Tokens tokens) {
        final String prefix = this.getPrefix(bookmark);
        if(StringUtils.isNotBlank(tokens.getAccessToken())) {
            keychain.addPassword(bookmark.getProtocol().getScheme(),
                    bookmark.getPort(), URI.create(tokenServerUrl).getHost(),
                    String.format("%s OAuth2 Access Token", prefix), tokens.getAccessToken());
        }
        if(StringUtils.isNotBlank(tokens.refreshToken)) {
            keychain.addPassword(bookmark.getProtocol().getScheme(),
                    bookmark.getPort(), URI.create(tokenServerUrl).getHost(),
                    String.format("%s OAuth2 Refresh Token", prefix), tokens.getRefreshToken());
        }
        // Save expiry
        if(tokens.expiryInMilliseconds != null) {
            preferences.setProperty(String.format("%s.oauth.expiry", bookmark.getProtocol().getIdentifier()), tokens.expiryInMilliseconds);
        }
    }

    private String getPrefix(final Host bookmark) {
        if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
            return String.format("%s (%s)", bookmark.getProtocol().getDescription(), bookmark.getCredentials().getUsername());
        }
        return bookmark.getProtocol().getDescription();
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

    private static final class TokenCredentials extends Credentials {
        private final Host bookmark;

        public TokenCredentials(final Host bookmark) {
            super(bookmark.getCredentials().getUsername());
            this.bookmark = bookmark;
        }

        @Override
        public String getUsernamePlaceholder() {
            return bookmark.getCredentials().getUsernamePlaceholder();
        }

        @Override
        public String getPasswordPlaceholder() {
            return bookmark.getCredentials().getPasswordPlaceholder();
        }
    }

    public static final class Tokens {
        public static final Tokens EMPTY = new Tokens(null, null, Long.MAX_VALUE);

        private String accessToken;
        private String refreshToken;
        private Long expiryInMilliseconds;

        public Tokens(final String accessToken, final String refreshToken, final Long expiryInMilliseconds) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiryInMilliseconds = expiryInMilliseconds;
        }

        public boolean validate() {
            return StringUtils.isNotEmpty(accessToken);
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public Long getExpiryInMilliseconds() {
            return expiryInMilliseconds;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expiryInMilliseconds;
        }
    }
}
