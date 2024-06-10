package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.JSON;
import ch.cyberduck.core.deepbox.io.swagger.client.api.UserRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Me;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.http.ChainedServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.jersey.HttpComponentsProvider;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.client.ClientBuilder;

public class DeepboxSession extends HttpSession<DeepboxApiClient> {
    private static final Logger log = LogManager.getLogger(DeepboxSession.class);
    private final DeepboxIdProvider fileid = new DeepboxIdProvider(this);

    private OAuth2RequestInterceptor authorizationService;

    private final PreferencesReader preferences = new HostPreferences(host);

    public DeepboxSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected DeepboxApiClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(builder.build(proxy, this, prompt).build(), host, prompt)
                .withFlowType(OAuth2AuthorizationService.FlowType.AuthorizationCode)
                .withRedirectUri(host.getProtocol().getOAuthRedirectUrl()
                );
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
                new ChainedServiceUnavailableRetryStrategy(new ExecutionCountServiceUnavailableRetryStrategy(
                        new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        configuration.addInterceptorLast(authorizationService);
        final CloseableHttpClient apache = configuration.build();
        final DeepboxApiClient client = new DeepboxApiClient(apache);
        client.setBasePath(new HostUrlProvider().withUsername(false).withPath(true).get(host.getProtocol().getScheme(), host.getPort(),
                null, host.getHostname(), host.getProtocol().getContext()));
        client.setHttpClient(ClientBuilder.newClient(new ClientConfig()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .register(new InputStreamProvider())
                .register(new JSON())
                .register(JacksonFeature.class)
                .connectorProvider(new HttpComponentsProvider(apache))));
        final int timeout = ConnectionTimeoutFactory.get(preferences).getTimeout() * 1000;
        client.setConnectTimeout(timeout);
        client.setReadTimeout(timeout);
        client.setUserAgent(new PreferencesUseragentProvider().get());
        return client;
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = authorizationService.validate();
        try {
            final Me me = new UserRestControllerApi(client).usersMe(null, null);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Authenticated for user %s", me));
            }
            credentials.setUsername(me.getEmail());
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(new DeepboxIdProvider(this)).map(e);
        }
    }

    @Override
    protected void logout() {
        client.getHttpClient().close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new DeepboxListService(this, fileid);
        }
        if(type == Directory.class) {
            return (T) new DeepboxDirectoryFeature(this, fileid);
        }
        if(type == Find.class) {
            return (T) new DeepboxFindFeature(this, fileid);
        }
        if(type == AttributesFinder.class) {
            return (T) new DeepboxAttributesFinderFeature(this, fileid);
        }
        if(type == Delete.class) {
            return (T) new DeepboxDeleteFeature(this, fileid);
        }
        return super._getFeature(type);
    }
}
