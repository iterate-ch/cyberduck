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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.openstack.SwiftExceptionMappingService;
import ch.cyberduck.core.openstack.SwiftSession;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;

public class HubicSession extends SwiftSession {
    private static final Logger log = LogManager.getLogger(HubicSession.class);

    private OAuth2RequestInterceptor authorizationService;

    public HubicSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected Client connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws ConnectionCanceledException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(configuration.build(), host, prompt)
                .withRedirectUri(host.getProtocol().getOAuthRedirectUrl());
        configuration.addInterceptorLast(authorizationService);
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        return new Client(configuration.build());
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = authorizationService.validate();
        try {
            log.info("Attempt authentication with {}", credentials.getOauth());
            client.authenticate(new HubicAuthenticationRequest(credentials.getOauth().getAccessToken()), new HubicAuthenticationResponseHandler());
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public <T> T _getFeature(final Class<T> type) {
        if(type == DistributionConfiguration.class) {
            return null;
        }
        return super._getFeature(type);
    }
}
