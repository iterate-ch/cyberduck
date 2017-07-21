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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
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
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.AuthApi;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.LoginResponse;
import ch.cyberduck.core.sds.provider.HttpComponentsProvider;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.message.internal.InputStreamProvider;

import javax.ws.rs.client.ClientBuilder;

public class SDSSession extends HttpSession<SDSApiClient> {

    private String token;

    final static String SDS_AUTH_TOKEN_HEADER = "X-Sds-Auth-Token";
    public static int DEFAULT_CHUNKSIZE = 16;

    private final SDSErrorResponseInterceptor retryHandler = new SDSErrorResponseInterceptor(this);

    public SDSSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected SDSApiClient connect(final HostKeyCallback key) throws BackgroundException {
        final HttpClientBuilder builder = this.builder.build(this);
        builder.setServiceUnavailableRetryStrategy(retryHandler);
        final CloseableHttpClient apache = builder.build();
        final SDSApiClient client = new SDSApiClient(apache);
        client.setBasePath(String.format("%s://%s%s", host.getProtocol().getScheme(), host.getHostname(), host.getProtocol().getContext()));
        final ClientConfig configuration = new ClientConfig();
        configuration.register(new InputStreamProvider());
        configuration.connectorProvider(new HttpComponentsProvider(this.builder, this));
        client.setHttpClient(ClientBuilder.newClient(configuration));
        client.setUserAgent(new PreferencesUseragentProvider().get());
        return client;
    }


    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        try {
            // The provided token is valid for two hours, every usage resets this period to two full hours again. Logging off invalidates the token.
            final String login = host.getCredentials().getUsername();
            final String password = host.getCredentials().getPassword();
            final LoginResponse response = new AuthApi(client).login(new LoginRequest()
                    .authType(host.getProtocol().getAuthorization())
                    .language("en")
                    .login(login)
                    .password(password)
            );
            token = response.getToken();
            // Save tokens for 401 error response when expired
            retryHandler.setTokens(login, password);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map(e);
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

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
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
            return (T) new SDSCopyFeature(this);
        }
        if(type == Bulk.class) {
            return (T) new SDSEncryptionBulkFeature(this);
        }
        return super._getFeature(type);
    }
}
