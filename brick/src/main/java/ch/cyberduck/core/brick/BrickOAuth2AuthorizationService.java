package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;

import java.net.URI;
import java.util.Map;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;

public class BrickOAuth2AuthorizationService extends OAuth2AuthorizationService {

    static final String ACCOUNT = "brick.oauth.username";
    private static final String API_KEY = "files_api_key";
    private static final String PASSWORD = "password";
    private static final String USERNAME = "user_username";
    private static final String SERVER = "server";
    private static final String NICKNAME = "nickname";

    private final Host host;

    public BrickOAuth2AuthorizationService(final HttpClient client, final Host host, final String clientid, final LoginCallback prompt) {
        this(new ApacheHttpTransport(client), host, clientid, prompt);
    }

    public BrickOAuth2AuthorizationService(final HttpTransport transport, final Host host, final String clientid, final LoginCallback prompt) {
        super(transport, host,
                toUrl(host, host.getProtocol().getOAuthTokenUrl()),
                toUrl(host, host.getProtocol().getOAuthAuthorizationUrl()),
                clientid, host.getProtocol().getOAuthClientSecret(), host.getProtocol().getOAuthScopes(), host.getProtocol().isOAuthPKCE(), prompt);
        this.host = host;
        this.setRedirectUri(host.getProtocol().getOAuthRedirectUrl());
        final String account = host.getCredentials().getProperty(ACCOUNT);
        if(StringUtils.isNotBlank(account)) {
            this.setParameter("login_hint", account);
        }
    }

    static boolean isKeychainEnabled() {
        return PreferencesFactory.get().getBoolean("connection.login.keychain");
    }

    private static String toUrl(final Host host, final String path) {
        return Scheme.isURL(path) ? path : String.format("%s%s",
                new HostUrlProvider().withUsername(false).withPath(false).get(host), path);
    }

    @Override
    protected IdTokenResponse exchangeToken(final AuthorizationCodeFlow flow, final String authorizationCode, final String redirectUri) throws BackgroundException {
        final IdTokenResponse response = super.exchangeToken(flow, authorizationCode, redirectUri);
        final Object value = response.get(API_KEY);
        if(!(value instanceof Map)) {
            throw new LoginFailureException("Missing Files.com API key in OAuth response");
        }
        this.configureCredentials((Map<?, ?>) value);
        return response;
    }

    private void configureCredentials(final Map<?, ?> key) throws LoginFailureException {
        final String password = this.required(key, PASSWORD);
        final String username = this.required(key, USERNAME);
        final URI server;
        try {
            server = URI.create(this.required(key, SERVER));
        }
        catch(IllegalArgumentException e) {
            throw new LoginFailureException("Invalid Files.com server in OAuth response", e);
        }
        if(!Scheme.https.name().equalsIgnoreCase(server.getScheme()) || StringUtils.isBlank(server.getHost())) {
            throw new LoginFailureException("Invalid Files.com server in OAuth response");
        }
        final int port = server.getPort() == -1 ? Scheme.https.getPort() : server.getPort();
        final String account = host.getCredentials().getProperty(ACCOUNT);
        if(StringUtils.isNotBlank(account) && !StringUtils.equals(account, username)) {
            throw new LoginFailureException(String.format("Sign out of Files.com in your browser, then sign in as %s on %s to reconnect this bookmark.",
                    account, host.getHostname()));
        }
        final boolean defaultOrigin = StringUtils.equalsIgnoreCase(host.getHostname(), host.getProtocol().getDefaultHostname())
                && host.getPort() == host.getProtocol().getDefaultPort();
        if((StringUtils.isNotBlank(account) || host.getCredentials().isPasswordAuthentication()) && !defaultOrigin
                && (!StringUtils.equalsIgnoreCase(host.getHostname(), server.getHost()) || host.getPort() != port)) {
            throw new LoginFailureException(String.format("Sign out of Files.com in your browser, then sign in on %s to reconnect this bookmark.",
                    host.getHostname()));
        }
        host.setHostname(server.getHost());
        host.setPort(port);
        host.getCredentials().setUsername(username).setPassword(password).setProperty(ACCOUNT, username)
                .setSaved(isKeychainEnabled() && new LoginOptions().save);
        if(HostPreferencesFactory.get(host).getBoolean("brick.pairing.nickname.configure") && key.get(NICKNAME) instanceof String) {
            host.setNickname((String) key.get(NICKNAME));
        }
    }

    private String required(final Map<?, ?> key, final String name) throws LoginFailureException {
        final Object value = key.get(name);
        if(!(value instanceof String) || StringUtils.isBlank((String) value)) {
            throw new LoginFailureException(String.format("Missing Files.com %s in OAuth response", name));
        }
        return (String) value;
    }
}
