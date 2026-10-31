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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;

public class BrickOAuth2AuthorizationService extends OAuth2AuthorizationService {
    private static final Logger log = LogManager.getLogger(BrickOAuth2AuthorizationService.class);

    private final Host host;
    private final BrickApiKeyRevocationService revocation;

    public BrickOAuth2AuthorizationService(final HttpClient client, final Host host, final String clientid, final LoginCallback prompt) {
        this(new ApacheHttpTransport(client), host, clientid, prompt);
    }

    public BrickOAuth2AuthorizationService(final HttpTransport transport, final Host host, final String clientid, final LoginCallback prompt) {
        super(transport, host,
                toUrl(host, host.getProtocol().getOAuthTokenUrl()),
                toUrl(host, host.getProtocol().getOAuthAuthorizationUrl()),
                clientid, host.getProtocol().getOAuthClientSecret(), host.getProtocol().getOAuthScopes(), host.getProtocol().isOAuthPKCE(), prompt);
        this.host = host;
        this.revocation = new BrickApiKeyRevocationService(transport);
        this.setRedirectUri(host.getProtocol().getOAuthRedirectUrl());
    }

    private static String toUrl(final Host host, final String path) {
        return Scheme.isURL(path) ? path : String.format("%s%s",
                new HostUrlProvider().withUsername(false).withPath(false).get(host), path);
    }

    @Override
    protected IdTokenResponse exchangeToken(final AuthorizationCodeFlow flow, final String authorizationCode, final String redirectUri) throws BackgroundException {
        final IdTokenResponse response = super.exchangeToken(flow, authorizationCode, redirectUri);
        // Files.com returns the REST API credential separately from the OAuth access token.
        final Object value = response.get("files_api_key");
        if(!(value instanceof Map)) {
            throw new LoginFailureException("Missing Files.com API key in OAuth response");
        }
        final Map<?, ?> key = (Map<?, ?>) value;
        try {
            this.configureCredentials(key);
        }
        catch(LoginFailureException e) {
            final Object password = key.get("password");
            if(password instanceof String && StringUtils.isNotBlank((String) password)) {
                try {
                    // Revoke at the token server, never at an address from a rejected response.
                    revocation.revoke(URI.create(flow.getTokenServerEncodedUrl()), (String) password);
                }
                catch(IOException cleanup) {
                    log.warn("Could not revoke rejected Files.com API key");
                }
            }
            throw e;
        }
        return response;
    }

    private void configureCredentials(final Map<?, ?> key) throws LoginFailureException {
        final String password = this.required(key, "password");
        final String username = this.required(key, "user_username");
        final String server = this.required(key, "server");
        final URI uri;
        try {
            uri = new URI(server);
        }
        catch(URISyntaxException e) {
            throw new LoginFailureException("Invalid Files.com server in OAuth response", e);
        }
        if(!StringUtils.equalsIgnoreCase(Scheme.https.name(), uri.getScheme()) || StringUtils.isBlank(uri.getHost())
                || uri.getPort() == 0 || uri.getPort() > 65535
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null
                || !StringUtils.equalsAny(uri.getPath(), "", "/")) {
            throw new LoginFailureException("Invalid Files.com server in OAuth response");
        }
        final int port = uri.getPort() == -1 ? Scheme.https.getPort() : uri.getPort();
        final boolean defaultOrigin = StringUtils.equalsIgnoreCase(host.getHostname(), "app.files.com") && host.getPort() == Scheme.https.getPort();
        final boolean sameOrigin = StringUtils.equalsIgnoreCase(host.getHostname(), uri.getHost()) && host.getPort() == port;
        if(StringUtils.isNotBlank(host.getCredentials().getUsername())
                && (!StringUtils.equals(host.getCredentials().getUsername(), username)
                || (!defaultOrigin && !sameOrigin))) {
            throw new LoginFailureException(String.format("Sign out of Files.com in your browser, then sign in as %s on %s to reconnect this bookmark.",
                    host.getCredentials().getUsername(), host.getHostname()));
        }
        final String nickname = HostPreferencesFactory.get(host).getBoolean("brick.pairing.nickname.configure") ?
                this.required(key, "nickname") : null;
        host.setHostname(uri.getHost());
        host.setPort(port);
        host.getCredentials().setUsername(username).setPassword(password).setSaved(new LoginOptions().save);
        if(nickname != null) {
            host.setNickname(nickname);
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
