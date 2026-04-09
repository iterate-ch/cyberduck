package ch.cyberduck.core.idgard;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.idgard.io.swagger.client.ApiException;
import ch.cyberduck.core.idgard.io.swagger.client.JSON;
import ch.cyberduck.core.idgard.io.swagger.client.api.AccountsApiApi;
import ch.cyberduck.core.idgard.io.swagger.client.model.Authentication;
import ch.cyberduck.core.idgard.io.swagger.client.model.SimpleUserInfo;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.jersey.HttpComponentsProvider;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DisabledMoveFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.client.ClientBuilder;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public class IdgardSession extends HttpSession<IdgardApiClient> {
    private static final Logger log = LogManager.getLogger(IdgardSession.class);

    //private final DeepboxIdProvider fileid = new DeepboxIdProvider(this);

    private IdgardApiClient idgardApiClient;

    public IdgardSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    private String token;

    @Override
    protected IdgardApiClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);

        //TODO für auth token?
        configuration.addInterceptorLast((HttpRequestInterceptor) (request, context) -> request.addHeader("x-idgard-csfr", token));

        final CloseableHttpClient apache = configuration.build();

        // Deepbox API client
        final IdgardApiClient client = new IdgardApiClient(apache);
        client.setBasePath(new HostUrlProvider().withUsername(false).withPath(true).get(host.getProtocol().getScheme(), host.getPort(),
                null, host.getHostname(), host.getProtocol().getContext()));
        client.setHttpClient(ClientBuilder.newClient(new ClientConfig()
                .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                .register(new InputStreamProvider())
                .register(new JSON())
                .register(JacksonFeature.class)
                .register(MultipartWrite.class)
                .connectorProvider(new HttpComponentsProvider(apache))));
        final int timeout = ConnectionTimeoutFactory.get(preferences).getTimeout() * 1000;
        client.setConnectTimeout(timeout);
        client.setReadTimeout(timeout);
        client.setUserAgent(new PreferencesUseragentProvider().get());

        return client;
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final Credentials credentials = host.getCredentials();
        try {
            final SimpleUserInfo userInfo = new AccountsApiApi(client).uiapiAccountsAPIV1RestLoginPost(
                    new Authentication().
                            username(credentials.getUsername()).
                            clientSecret("blabla").
                            password(credentials.getPassword()).
                            verifyToken("blabla").
                            version("blabla")
            );
            token = userInfo.getCsfrToken();
        }
        catch(ApiException e) {
            throw new BackgroundException(e);
        }
    }

    @Override
    public void disconnect() throws BackgroundException {
        try {
            if(client != null) {
                client.getHttpClient().close();
            }
        }
        finally {
            super.disconnect();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
//        if(type == FileIdProvider.class) {
//            return (T) fileid;
//        }
        if(type == ListService.class) {
            return (T) new IdgardListService(this);
        }
        if(type == Touch.class) {
            return (T) new DefaultTouchFeature<>(this);
        }
        if(type == Read.class) {
            return (T) new Read() {
                @Override
                public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                    return new NullInputStream(0L);
                }

                @Override
                public boolean offset(final Path file) throws BackgroundException {
                    return false;
                }
            };
        }
        if(type == Write.class) {
            return (T) new Write<Void>() {
                @Override
                public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) {
                    return new VoidStatusOutputStream(NullOutputStream.INSTANCE);
                }
            };
        }
        if(type == Directory.class) {
            return (T) new Directory() {
                @Override
                public Path mkdir(final Write writer, final Path folder, final TransferStatus status) throws BackgroundException {
                    return folder;
                }
            };
        }
        if(type == Move.class) {
            return (T) new DisabledMoveFeature() {
                @Override
                public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
                    //
                }
            };
        }
        if (type == Delete.class){
            return (T) new Delete(){
                @Override
                public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
                    //
                }
            };
        }
        return super._getFeature(type);
    }
}
