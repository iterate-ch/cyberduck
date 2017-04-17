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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.local.BrowserLauncher;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
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

    public Credential authorize(final Host bookmark, final HostPasswordStore keychain,
                                final LoginCallback prompt, final CancelCallback cancel, final Tokens saved) throws BackgroundException {
        final Credential tokens;
        if(saved.validate()) {
            tokens = new Credential.Builder(method)
                    .setTransport(transport)
                    .setClientAuthentication(new ClientParametersAuthentication(clientid, clientsecret))
                    .setTokenServerEncodedUrl(tokenServerUrl)
                    .setJsonFactory(json)
                    .addRefreshListener(new SavingCredentialRefreshListener(keychain, bookmark))
                    .build()
                    .setAccessToken(saved.accesstoken)
                    .setRefreshToken(saved.refreshtoken)
                    .setExpirationTimeMilliseconds(saved.expiry);
            if(this.isExpired(tokens)) {
                this.refresh(tokens);
            }
        }
        else {
            final Credentials token = new TokenCredentials(bookmark);
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
            // Direct the user to an authorization page to grant access to their protected data.
            final String url = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();
            if(!browser.open(url)) {
                log.warn(String.format("Failed to launch web browser for %s", url));
            }
            if(StringUtils.equals(CYBERDUCK_REDIRECT_URI, redirectUri)) {
                final OAuth2TokenListenerRegistry registry = OAuth2TokenListenerRegistry.get();
                registry.register(new OAuth2TokenListener() {
                    @Override
                    public void callback(final String param) {
                        token.setPassword(param);
                    }
                }, cancel);
            }
            else {
                prompt.prompt(bookmark, token,
                        LocaleFactory.localizedString("OAuth2 Authentication", "Credentials"),
                        LocaleFactory.localizedString("Paste the authentication code from your web browser", "Credentials"),
                        new LoginOptions().keychain(false).user(false).password(true)
                );
            }
            try {
                if(StringUtils.isBlank(token.getPassword())) {
                    throw new LoginCanceledException();
                }
                // Swap the given authorization token for access/refresh tokens
                final TokenResponse response = flow.newTokenRequest(token.getPassword())
                        .setRedirectUri(redirectUri).setScopes(scopes.isEmpty() ? null : scopes).execute();
                tokens = new Credential.Builder(method)
                        .setTransport(transport)
                        .setClientAuthentication(new ClientParametersAuthentication(clientid, clientsecret))
                        .setTokenServerEncodedUrl(tokenServerUrl)
                        .setJsonFactory(json)
                        .addRefreshListener(new SavingCredentialRefreshListener(keychain, bookmark))
                        .build()
                        .setFromTokenResponse(response);

                // Save
                save(keychain, bookmark, new Tokens(
                        tokens.getAccessToken(), tokens.getRefreshToken(), tokens.getExpirationTimeMilliseconds()));
            }
            catch(IOException e) {
                throw new OAuthExceptionMappingService().map(e);
            }
        }
        return tokens;
    }

    public void refresh(final Credential tokens) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Refresh expired tokens %s", tokens));
        }
        try {
            tokens.refreshToken();
        }
        catch(IOException e) {
            throw new OAuthExceptionMappingService().map(e);
        }
    }

    public Tokens find(final HostPasswordStore keychain, final Host bookmark) {
        final long expiry = preferences.getLong(String.format("%s.oauth.expiry", bookmark.getProtocol().getIdentifier()));
        final String prefix;
        if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
            prefix = String.format("%s (%s)", bookmark.getProtocol().getDescription(), bookmark.getCredentials().getUsername());
        }
        else {
            prefix = bookmark.getProtocol().getDescription();
        }
        return new Tokens(keychain.getPassword(bookmark.getProtocol().getScheme(),
                bookmark.getPort(), URI.create(tokenServerUrl).getHost(),
                String.format("%s OAuth2 Access Token", prefix)),
                keychain.getPassword(bookmark.getProtocol().getScheme(),
                        bookmark.getPort(), URI.create(tokenServerUrl).getHost(),
                        String.format("%s OAuth2 Refresh Token", prefix)),
                expiry == -1 ? Long.MAX_VALUE : expiry);
    }

    private void save(final HostPasswordStore keychain, final Host bookmark, final Tokens tokens) {
        final String prefix = String.format("%s (%s)", bookmark.getProtocol().getDescription(), bookmark.getCredentials().getUsername());
        if(StringUtils.isNotBlank(tokens.accesstoken)) {
            keychain.addPassword(bookmark.getProtocol().getScheme(),
                    bookmark.getPort(), URI.create(tokenServerUrl).getHost(),
                    String.format("%s OAuth2 Access Token", prefix), tokens.accesstoken);
        }
        if(StringUtils.isNotBlank(tokens.refreshtoken)) {
            keychain.addPassword(bookmark.getProtocol().getScheme(),
                    bookmark.getPort(), URI.create(tokenServerUrl).getHost(),
                    String.format("%s OAuth2 Refresh Token", prefix), tokens.refreshtoken);
        }
        // Save expiry
        if(tokens.expiry != null) {
            preferences.setProperty(String.format("%s.oauth.expiry", bookmark.getProtocol().getIdentifier()), tokens.expiry);
        }
    }

    private String getPrefix(final Host host) {
        return String.format("%s (%s)", host.getProtocol().getDescription(), host.getCredentials().getUsername());
    }

    public OAuth2AuthorizationService withMethod(final Credential.AccessMethod method) {
        this.method = method;
        return this;
    }

    public OAuth2AuthorizationService withRedirectUri(final String redirectUri) {
        this.redirectUri = redirectUri;
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

        public final String accesstoken;
        public final String refreshtoken;
        public final Long expiry;

        public Tokens(final String accesstoken, final String refreshtoken, final Long expiry) {
            this.accesstoken = accesstoken;
            this.refreshtoken = refreshtoken;
            this.expiry = expiry;
        }

        public boolean validate() {
            return StringUtils.isNotEmpty(accesstoken);
        }
    }

    private static final class OAuthExceptionMappingService extends DefaultIOExceptionMappingService {
        @Override
        public BackgroundException map(final IOException failure) {
            if(failure instanceof TokenResponseException) {
                final TokenErrorResponse details = ((TokenResponseException) failure).getDetails();
                final StringBuilder buffer = new StringBuilder();
                this.append(buffer, details.getErrorDescription());
                return new LoginFailureException(buffer.toString(), failure);
            }
            final StringBuilder buffer = new StringBuilder();
            if(failure instanceof HttpResponseException) {
                final HttpResponseException response = (HttpResponseException) failure;
                this.append(buffer, response.getStatusMessage());
                if(response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    // Invalid Credentials. Refresh the access token using the long-lived refresh token
                    return new LoginFailureException(buffer.toString(), failure);
                }
                if(response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                    // Invalid Grant
                    return new LoginFailureException(buffer.toString(), failure);
                }
                if(response.getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                    return new AccessDeniedException(buffer.toString(), failure);
                }
                if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                    return new NotfoundException(buffer.toString(), failure);
                }
            }
            return super.map(failure);
        }
    }

    private final class SavingCredentialRefreshListener implements CredentialRefreshListener {
        private final Host host;
        private final HostPasswordStore keychain;

        public SavingCredentialRefreshListener(final HostPasswordStore keychain, final Host bookmark) {
            this.keychain = keychain;
            this.host = bookmark;
        }

        @Override
        public void onTokenResponse(final Credential credential, final TokenResponse tokenResponse) throws IOException {
            save(keychain, host, new Tokens(credential.getAccessToken(), credential.getRefreshToken(),
                    credential.getExpirationTimeMilliseconds()));
        }

        @Override
        public void onTokenErrorResponse(final Credential credential, final TokenErrorResponse tokenErrorResponse) throws IOException {
            log.warn(String.format("Failure with OAuth2 token response %s", null == tokenErrorResponse ? "Unknown" : tokenErrorResponse.getError()));
        }
    }

    public boolean isExpired(final Credential tokens) {
        return tokens.getExpirationTimeMilliseconds() >= System.currentTimeMillis();
    }
}
