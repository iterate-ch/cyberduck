package ch.cyberduck.core.hubic;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.openstack.SwiftExceptionMappingService;
import ch.cyberduck.core.openstack.SwiftSession;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.util.Collections;

import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import com.google.api.client.auth.oauth2.Credential;

public class HubicSession extends SwiftSession {
    private static final Logger log = Logger.getLogger(HubicSession.class);
    public final OAuth2AuthorizationService authorizationService = new OAuth2AuthorizationService(this,
            "https://api.hubic.com/oauth/token",
            "https://api.hubic.com/oauth/auth",
            PreferencesFactory.get().getProperty("hubic.oauth.clientid"),
            PreferencesFactory.get().getProperty("hubic.oauth.secret"),
            Collections.singletonList("credentials.r")
    ).withRedirectUri("https://cyberduck.io/oauth");

    public HubicSession(final Host host) {
        super(host);
    }

    public HubicSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public HubicSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        super(host, trust, key, proxy);
    }

    public HubicSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final SocketFactory socketFactory) {
        super(host, trust, key, socketFactory);
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        final Credential tokens = authorizationService.authorize(keychain, prompt);
        try {
            try {
                client.authenticate(new HubicAuthenticationRequest(tokens.getAccessToken()),
                        new HubicAuthenticationResponseHandler());
            }
            catch(AuthorizationException e) {
                authorizationService.refresh(tokens);
                client.authenticate(new HubicAuthenticationRequest(tokens.getAccessToken()),
                        new HubicAuthenticationResponseHandler());
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
