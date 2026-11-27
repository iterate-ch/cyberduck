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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProvider;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProviderFactory;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;

public class BrickOAuth2AuthorizationService extends OAuth2RequestInterceptor {
    static final String VERIFIED = "brick.oauth.verified";
    static final String REST_ORIGIN = "brick.rest.origin";
    static final String API_KEY_HEADER = "X-FilesAPI-Key";
    private static final String API_KEY = "files_api_key";
    private static final String PASSWORD = "password";
    private static final String USERNAME = "user_username";
    private static final String SERVER = "server";

    private final Host host;
    private final HostPasswordStore store = PasswordStoreFactory.get();

    public BrickOAuth2AuthorizationService(final HttpClient client, final Host host, final LoginCallback prompt, final CancelCallback cancel) {
        this(new ApacheHttpTransport(client), host, prompt, cancel, OAuth2AuthorizationCodeProviderFactory.get());
    }

    BrickOAuth2AuthorizationService(final HttpTransport transport, final Host host, final LoginCallback prompt,
                                    final CancelCallback cancel, final OAuth2AuthorizationCodeProvider provider) {
        super(transport, host, toUrl(host, host.getProtocol().getOAuthTokenUrl()), toUrl(host, host.getProtocol().getOAuthAuthorizationUrl()),
                host.getProtocol().getOAuthClientId(), host.getProtocol().getOAuthClientSecret(), host.getProtocol().getOAuthScopes(),
                host.getProtocol().isOAuthPKCE(), prompt, cancel, provider);
        this.host = host;
        this.setRedirectUri(host.getProtocol().getOAuthRedirectUrl());
    }

    @Override
    public OAuthTokens validate(final OAuthTokens saved, final CancelCallback cancel) throws BackgroundException {
        if(saved.validate()) {
            host.setProperty(VERIFIED, Boolean.TRUE.toString());
            return super.validate(saved, cancel);
        }
        if(!Boolean.parseBoolean(host.getProperty(VERIFIED)) && new LoginOptions().keychain) {
            final String password = store.findLoginPassword(host);
            if(StringUtils.isNotBlank(password)) {
                final OAuthTokens migrated = this.save(new OAuthTokens(password, null, Long.MAX_VALUE));
                host.setProperty(VERIFIED, Boolean.TRUE.toString());
                return migrated;
            }
        }
        return this.save(super.validate(saved, cancel));
    }

    @Override
    public OAuthTokens authorize(final CancelCallback cancel) throws BackgroundException {
        try {
            this.setParameter("computer", InetAddress.getLocalHost().getHostName());
        }
        catch(UnknownHostException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        this.setTokenServerUrl(toUrl(host, host.getProtocol().getOAuthTokenUrl()));
        this.setAuthorizationServerUrl(toUrl(host, host.getProtocol().getOAuthAuthorizationUrl()));
        return super.authorize(cancel);
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) {
        request.removeHeaders(API_KEY_HEADER);
        final HttpHost target = HttpCoreContext.adapt(context).getTargetHost();
        final boolean api = target != null && StringUtils.startsWith(URI.create(request.getRequestLine().getUri()).getPath(), "/api/rest/v1/")
                && StringUtils.equalsIgnoreCase(target.getHostName(), host.getHostname())
                && StringUtils.equalsIgnoreCase(target.getSchemeName(), host.getProtocol().getScheme().name())
                && (target.getPort() == -1 ? host.getProtocol().getScheme().getPort() : target.getPort()) == host.getPort();
        // Send the API key only to the bookmark's REST API, never to transfer hosts reached by redirect.
        // Record each hop before sending so only REST 401s trigger sign-in, even if the bookmark changes
        // while the response is in flight.
        context.setAttribute(REST_ORIGIN, api);
        if(api) {
            final Credentials credentials = host.getCredentials();
            final String key = credentials.isPasswordAuthentication() ? credentials.getPassword() : credentials.getOauth().getAccessToken();
            if(StringUtils.isNotBlank(key)) {
                request.addHeader(API_KEY_HEADER, key);
            }
        }
    }

    private static String toUrl(final Host host, final String path) {
        return Scheme.isURL(path) ? path : String.format("%s%s", new HostUrlProvider().withUsername(false).withPath(false).get(host), path);
    }

    @Override
    protected IdTokenResponse exchangeToken(final AuthorizationCodeFlow flow, final String authorizationCode, final String redirectUri) throws BackgroundException {
        final IdTokenResponse response = super.exchangeToken(flow, authorizationCode, redirectUri);
        final Object value = response.get(API_KEY);
        if(!(value instanceof Map)) {
            throw new InteroperabilityException("Missing Files.com API key in OAuth response");
        }
        final Map<?, ?> key = (Map<?, ?>) value;
        final String password = this.required(key, PASSWORD);
        final String username = this.required(key, USERNAME);
        final URI server;
        try {
            server = URI.create(this.required(key, SERVER));
        }
        catch(IllegalArgumentException e) {
            throw new InteroperabilityException("Invalid Files.com server in OAuth response", e);
        }
        if(!Scheme.https.name().equalsIgnoreCase(server.getScheme()) || StringUtils.isBlank(server.getHost())) {
            throw new InteroperabilityException("Invalid Files.com server in OAuth response");
        }
        final int port = server.getPort() == -1 ? Scheme.https.getPort() : server.getPort();
        final boolean verified = Boolean.parseBoolean(host.getProperty(VERIFIED));
        final boolean defaultOrigin = StringUtils.equalsIgnoreCase(host.getHostname(), host.getProtocol().getDefaultHostname())
                && host.getPort() == host.getProtocol().getDefaultPort();
        if(verified && !defaultOrigin && (!StringUtils.equalsIgnoreCase(host.getHostname(), server.getHost()) || host.getPort() != port)) {
            throw new LoginFailureException(String.format("Sign out of Files.com in your browser, then sign in on %s to reconnect this bookmark.", host.getHostname()));
        }
        host.setHostname(server.getHost());
        host.setPort(port);
        host.getCredentials().setUsername(username);
        host.setProperty(VERIFIED, Boolean.TRUE.toString());
        // REST uses the desktop API key; the short-lived OAuth tokens are not used.
        return new IdTokenResponse().setAccessToken(password);
    }

    private String required(final Map<?, ?> key, final String name) throws InteroperabilityException {
        final Object value = key.get(name);
        if(!(value instanceof String) || StringUtils.isBlank((String) value)) {
            throw new InteroperabilityException(String.format("Missing Files.com %s in OAuth response", name));
        }
        return (String) value;
    }
}
