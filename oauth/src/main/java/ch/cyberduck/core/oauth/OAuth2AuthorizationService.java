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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.http.HttpConnectionPoolBuilder;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.local.BrowserLauncher;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

public class OAuth2AuthorizationService {

    private final Preferences preferences
            = PreferencesFactory.get();

    private final JsonFactory json = new GsonFactory();

    private final String tokenServerUrl;

    private final String authorizationServerUrl;

    private final String clientid;

    private final String clientsecret;

    public final BrowserLauncher browser = BrowserLauncherFactory.get();

    private Credential.AccessMethod method = BearerToken.authorizationHeaderAccessMethod();

    private static final String OOB_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    private final List<String> scopes;

    /**
     * Prefix for saved entries in keychain.
     */
    private String legacyPrefix;

    public OAuth2AuthorizationService(final String tokenServerUrl, final String authorizationServerUrl,
                                      final String clientid, final String clientsecret, final List<String> scopes) {
        this.tokenServerUrl = tokenServerUrl;
        this.authorizationServerUrl = authorizationServerUrl;
        this.clientid = clientid;
        this.clientsecret = clientsecret;
        this.scopes = scopes;
    }

    public Credential authorize(final HttpSession<?> session, final HostPasswordStore keychain,
                                final LoginCallback prompt) throws BackgroundException {

        final HttpConnectionPoolBuilder pool = session.getBuilder();
        final ApacheHttpTransport transport = new ApacheHttpTransport(pool.build(session).build());
        final Host host = session.getHost();
        final Tokens saved = this.find(keychain, host);
        final Credential tokens;
        if(saved.validate()) {
            tokens = new Credential.Builder(method)
                    .setTransport(transport)
                    .setClientAuthentication(new ClientParametersAuthentication(clientid, clientsecret))
                    .setTokenServerEncodedUrl(tokenServerUrl)
                    .setJsonFactory(json).build()
                    .setAccessToken(saved.accesstoken)
                    .setRefreshToken(saved.refreshtoken)
                    .setExpirationTimeMilliseconds(saved.expiry);
        }
        else {
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
            final String url = flow.newAuthorizationUrl().setRedirectUri(OOB_REDIRECT_URI).build();
            browser.open(url);
            prompt.prompt(host, host.getCredentials(),
                    LocaleFactory.localizedString("OAuth2 Authentication", "Credentials"), url,
                    new LoginOptions().keychain(false).user(false)
            );
            try {
                // Swap the given authorization token for access/refresh tokens
                final TokenResponse response = flow.newTokenRequest(host.getCredentials().getPassword())
                        .setRedirectUri(OOB_REDIRECT_URI).execute();
                tokens = new Credential.Builder(method)
                        .setTransport(transport)
                        .setClientAuthentication(new ClientParametersAuthentication(clientid, clientsecret))
                        .setTokenServerEncodedUrl(tokenServerUrl)
                        .setJsonFactory(json).build()
                        .setFromTokenResponse(response);

                // Save for future use
                this.save(keychain, host, new Tokens(tokens.getAccessToken(), tokens.getRefreshToken(),
                        tokens.getExpirationTimeMilliseconds()));
            }
            catch(IOException e) {
                throw new OAuthExceptionMappingService().map(e);
            }
        }
        return tokens;
    }

    protected Tokens find(final HostPasswordStore keychain, final Host host) {
        final long expiry = preferences.getLong(String.format("%s.oauth.expiry", host.getProtocol().getIdentifier()));
        final Tokens tokens = new Tokens(keychain.getPassword(host.getProtocol().getScheme(),
                host.getPort(), URI.create(tokenServerUrl).getHost(),
                String.format("%s OAuth2 Access Token", String.format("%s (%s)", host.getProtocol().getDescription(), host.getCredentials().getUsername()))),
                keychain.getPassword(host.getProtocol().getScheme(),
                        host.getPort(), URI.create(tokenServerUrl).getHost(),
                        String.format("%s OAuth2 Refresh Token", String.format("%s (%s)", host.getProtocol().getDescription(), host.getCredentials().getUsername()))),
                expiry);
        if(!tokens.validate()) {
            if(legacyPrefix != null) {
                // Not found
                return new Tokens(keychain.getPassword(host.getProtocol().getScheme(),
                        host.getPort(), URI.create(tokenServerUrl).getHost(),
                        String.format("%s OAuth2 Access Token", legacyPrefix)),
                        keychain.getPassword(host.getProtocol().getScheme(),
                                host.getPort(), URI.create(tokenServerUrl).getHost(),
                                String.format("%s OAuth2 Refresh Token", legacyPrefix)), expiry);
            }
        }
        return tokens;
    }

    protected void save(final HostPasswordStore keychain, final Host host, final Tokens tokens) {
        final String prefix = String.format("%s (%s)", host.getProtocol().getDescription(), host.getCredentials().getUsername());
        keychain.addPassword(host.getProtocol().getScheme(),
                host.getPort(), URI.create(tokenServerUrl).getHost(),
                String.format("%s OAuth2 Access Token", prefix), tokens.accesstoken);
        keychain.addPassword(host.getProtocol().getScheme(),
                host.getPort(), URI.create(tokenServerUrl).getHost(),
                String.format("%s OAuth2 Refresh Token", prefix), tokens.refreshtoken);
        // Save expiry
        preferences.setProperty(String.format("%s.oauth.expiry", host.getProtocol().getIdentifier()), tokens.expiry);
    }

    private String getPrefix(final Host host) {
        final String prefix;
        if(null == legacyPrefix) {
            prefix = String.format("%s (%s)", host.getProtocol().getDescription(), host.getCredentials().getUsername());
        }
        else {
            prefix = legacyPrefix;
        }
        return prefix;
    }

    public OAuth2AuthorizationService withMethod(final Credential.AccessMethod method) {
        this.method = method;
        return this;
    }

    /**
     * @param identifier Prefix in saved keychain entries
     */
    public OAuth2AuthorizationService withLegacyPrefix(final String identifier) {
        this.legacyPrefix = identifier;
        return this;
    }

    private final class Tokens {
        public final String accesstoken;
        public final String refreshtoken;
        public final Long expiry;

        public Tokens(final String accesstoken, final String refreshtoken, final Long expiry) {
            this.accesstoken = accesstoken;
            this.refreshtoken = refreshtoken;
            this.expiry = expiry;
        }

        public boolean validate() {
            return StringUtils.isNotEmpty(accesstoken) && StringUtils.isNotEmpty(refreshtoken);
        }
    }

    private final class OAuthExceptionMappingService extends DefaultIOExceptionMappingService {
        @Override
        public BackgroundException map(final IOException failure) {
            if(failure instanceof TokenResponseException) {
                final TokenErrorResponse details = ((TokenResponseException) failure).getDetails();
                final StringBuilder buffer = new StringBuilder();
                this.append(buffer, details.getError());
                return new LoginFailureException(buffer.toString(), failure);
            }
            final StringBuilder buffer = new StringBuilder();
            if(failure instanceof HttpResponseException) {
                final HttpResponseException response = (HttpResponseException) failure;
                this.append(buffer, response.getStatusMessage());
                if(response.getStatusCode() == 401) {
                    // Invalid Credentials. Refresh the access token using the long-lived refresh token
                    return new LoginFailureException(buffer.toString(), failure);
                }
                if(response.getStatusCode() == 403) {
                    return new AccessDeniedException(buffer.toString(), failure);
                }
                if(response.getStatusCode() == 404) {
                    return new NotfoundException(buffer.toString(), failure);
                }
            }
            return super.map(failure);
        }
    }
}
