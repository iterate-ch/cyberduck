package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.JSON;
import ch.cyberduck.core.storegate.io.swagger.client.api.UsersApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.ExtendedUser;
import ch.cyberduck.core.storegate.provider.HttpComponentsProvider;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.client.ClientBuilder;
import java.nio.charset.StandardCharsets;

import com.migcomponents.migbase64.Base64;

public class StoregateSession extends HttpSession<StoregateApiClient> {
    private static final Logger log = Logger.getLogger(StoregateSession.class);

    protected OAuth2RequestInterceptor authorizationService;

    private final StoregateIdProvider fileid = new StoregateIdProvider(this);

    private String username;

    public StoregateSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected StoregateApiClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(builder.build(proxy, this, prompt).addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                request.addHeader(HttpHeaders.AUTHORIZATION,
                    String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", host.getProtocol().getOAuthClientId(), host.getProtocol().getOAuthClientSecret()).getBytes(StandardCharsets.UTF_8), false)));
            }
        }).build(),
            host).withRedirectUri(Scheme.isURL(host.getProtocol().getOAuthRedirectUrl()) ? host.getProtocol().getOAuthRedirectUrl() : new HostUrlProvider().withUsername(false).withPath(true).get(
            host.getProtocol().getScheme(), host.getPort(), null, host.getHostname(), host.getProtocol().getOAuthRedirectUrl())
        );
        configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService, prompt));
        configuration.addInterceptorLast(authorizationService);


        final CloseableHttpClient apache = configuration.build();
        final StoregateApiClient client = new StoregateApiClient(apache);
        client.setBasePath(new HostUrlProvider().withUsername(false).withPath(true).get(host.getProtocol().getScheme(), host.getPort(),
            null, host.getHostname(), host.getProtocol().getContext()));
        client.setHttpClient(ClientBuilder.newClient(new ClientConfig()
            .register(new InputStreamProvider())
            .register(MultiPartFeature.class)
            .register(new JSON())
            .register(JacksonFeature.class)
            .connectorProvider(new HttpComponentsProvider(apache))));
        client.setUserAgent(new PreferencesUseragentProvider().get());
        return client;
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback controller, final CancelCallback cancel) throws BackgroundException {
        authorizationService.setTokens(authorizationService.authorize(host, controller, cancel));
        try {
            final ExtendedUser me = new UsersApi(this.client).usersGetMe();
            username = me.getUsername();
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService().map(e);
        }
    }

    public String username() {
        return username;
    }

    @Override
    protected void logout() {
        client.getHttpClient().close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == IdProvider.class) {
            return (T) fileid;
        }
        if(type == ListService.class) {
            return (T) new StoregateListService(this);
        }
        if(type == Read.class) {
            return (T) new StoregateReadFeature(this, fileid);
        }
        if(type == Write.class) {
            return (T) new StoregateWriteFeature(this, fileid);
        }
        if(type == MultipartWrite.class) {
            return (T) new StoregateMultipartWriteFeature(this, fileid);
        }
        if(type == Touch.class) {
            return (T) new StoregateTouchFeature(this, fileid);
        }
        if(type == Move.class) {
            return (T) new StoregateMoveFeature(this, fileid);
        }
        if(type == Copy.class) {
            return (T) new StoregateCopyFeature(this, fileid);
        }
        if(type == Directory.class) {
            return (T) new StoregateDirectoryFeature(this, fileid);
        }
        if(type == Delete.class) {
            return (T) new StoregateDeleteFeature(this, fileid);
        }
        if(type == AttributesFinder.class) {
            return (T) new StoregateAttributesFinderFeature(this);
        }
        return super._getFeature(type);
    }
}
