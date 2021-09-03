package ch.cyberduck.core.gmxcloud;/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.jersey.HttpComponentsProvider;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.nio.charset.StandardCharsets;

import com.migcomponents.migbase64.Base64;

public class GmxcloudSession extends HttpSession<Client> {
    private static final Logger log = Logger.getLogger(GmxcloudSession.class);

    private OAuth2RequestInterceptor authorizationService;

    public GmxcloudSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected Client connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(builder.build(proxy, this, prompt).addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                request.addHeader(HttpHeaders.AUTHORIZATION,
                    String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", host.getProtocol().getOAuthClientId(), host.getProtocol().getOAuthClientSecret()).getBytes(StandardCharsets.UTF_8), false)));
            }
        }).build(), host)
            .withRedirectUri(host.getProtocol().getOAuthRedirectUrl()
            );
        configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService, prompt));
        configuration.addInterceptorLast(authorizationService);
        final CloseableHttpClient apache = configuration.build();
        return ClientBuilder.newClient(new ClientConfig()
            .register(new InputStreamProvider())
            .register(MultiPartFeature.class)
//            .register(new JSON())
            .register(JacksonFeature.class)
            .connectorProvider(new HttpComponentsProvider(apache)));
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final OAuthTokens tokens = authorizationService.refresh(authorizationService.authorize(host, prompt, cancel, OAuth2AuthorizationService.FlowType.AuthorizationCode));
        authorizationService.setTokens(tokens);

        // TODO service target auflösen via "https://os-mc.ui-onlinestorage.net/serviceTarget/onlinestorage.qa.mc"
        // curl -v -Hx-ui-api-key:$apikey -Hx-ui-app:curl/1 -H"Authorization: Bearer $accessToken" https://os-webde.ui-onlinestorage.net/serviceTarget/onlinestorage.qa.webde
    }

    @Override
    protected void logout() throws BackgroundException {
        client.close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        return super.getFeature(type);
    }
}
