package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.PartialLoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Background;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.AuthApi;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginRequest;
import ch.cyberduck.core.sds.provider.HttpComponentsProvider;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

import com.migcomponents.migbase64.Base64;

public class SDSSession extends HttpSession<SDSApiClient> {

    public static final String SDS_AUTH_TOKEN_HEADER = "X-Sds-Auth-Token";
    public static final int DEFAULT_CHUNKSIZE = 16;

    private final SDSErrorResponseInterceptor retryHandler = new SDSErrorResponseInterceptor(this);

    private final OAuth2RequestInterceptor authorizationService
            = new OAuth2RequestInterceptor(builder.build(this).addInterceptorLast(new HttpRequestInterceptor() {
        @Override
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            request.addHeader(HttpHeaders.AUTHORIZATION,
                    String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", host.getProtocol().getOAuthClientId(), host.getProtocol().getOAuthClientSecret()).getBytes("UTF-8"), false)));
        }
    }).build(),
            host.getProtocol()).withRedirectUri(host.getProtocol().getOAuthRedirectUrl());

    public SDSSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected SDSApiClient connect(final HostKeyCallback key) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(this);
        switch(SDSProtocol.Authorization.valueOf(host.getProtocol().getAuthorization())) {
            case oauth:
                configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(authorizationService));
                configuration.addInterceptorLast(authorizationService);
                configuration.addInterceptorLast(new HttpRequestInterceptor() {
                    @Override
                    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                        request.removeHeaders(SDSSession.SDS_AUTH_TOKEN_HEADER);
                    }
                });
                break;
            default:
                configuration.setServiceUnavailableRetryStrategy(retryHandler);
                configuration.addInterceptorLast(retryHandler);
                break;
        }
        final CloseableHttpClient apache = configuration.build();
        final SDSApiClient client = new SDSApiClient(apache);
        client.setBasePath(String.format("%s://%s%s", host.getProtocol().getScheme(), host.getHostname(), host.getProtocol().getContext()));
        client.setHttpClient(ClientBuilder.newClient(new ClientConfig()
                .register(new InputStreamProvider())
                .connectorProvider(new HttpComponentsProvider(apache))));
        client.setUserAgent(new PreferencesUseragentProvider().get());
        return client;
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback controller, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        final String login = host.getCredentials().getUsername();
        final String password = host.getCredentials().getPassword();
        // The provided token is valid for two hours, every usage resets this period to two full hours again. Logging off invalidates the token.
        switch(SDSProtocol.Authorization.valueOf(host.getProtocol().getAuthorization())) {
            case oauth:
                authorizationService.setTokens(authorizationService.authorize(host, keychain, controller, cancel));
                break;
            default:
                // Save tokens for 401 error response when expired
                retryHandler.setTokens(login, password, this.login(controller, new LoginRequest()
                        .authType(host.getProtocol().getAuthorization())
                        .language("en")
                        .login(login)
                        .password(password)
                ));
                break;
        }
    }

    private String login(final LoginCallback controller, final LoginRequest request) throws BackgroundException {
        try {
            try {
                return new AuthApi(client).login(request).getToken();
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService().map(e);
            }
        }
        catch(PartialLoginFailureException e) {
            final Credentials additional = new Credentials(host.getCredentials().getUsername());
            controller.prompt(host, additional, LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                    e.getDetail(), new LoginOptions().user(false).keychain(false)
            );
            return this.login(controller, new LoginRequest()
                    .authType(host.getProtocol().getAuthorization())
                    .language("en")
                    .token(additional.getPassword())
            );
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        client.getHttpClient().close();
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new SDSListService(this).list(directory, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Read.class) {
            return (T) new SDSDelegatingReadFeature(this, new SDSReadFeature(this));
        }
        if(type == Write.class) {
            return (T) new SDSDelegatingWriteFeature(this, new SDSWriteFeature(this));
        }
        if(type == MultipartWrite.class) {
            return (T) new SDSDelegatingWriteFeature(this, new SDSMultipartWriteFeature(this));
        }
        if(type == Directory.class) {
            return (T) new SDSDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new SDSDeleteFeature(this);
        }
        if(type == IdProvider.class) {
            return (T) new SDSNodeIdProvider(this);
        }
        if(type == Touch.class) {
            return (T) new SDSTouchFeature(this);
        }
        if(type == Find.class) {
            return (T) new SDSFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new SDSAttributesFinderFeature(this);
        }
        if(type == Move.class) {
            return (T) new SDSDelegatingMoveFeature(this, new SDSMoveFeature(this));
        }
        if(type == Copy.class) {
            return (T) new SDSDelegatingCopyFeature(this, new SDSCopyFeature(this));
        }
        if(type == Bulk.class) {
            return (T) new SDSEncryptionBulkFeature(this);
        }
        if(type == Background.class) {
            return (T) new SDSBackgroundFeature(this);
        }
        return super._getFeature(type);
    }
}
