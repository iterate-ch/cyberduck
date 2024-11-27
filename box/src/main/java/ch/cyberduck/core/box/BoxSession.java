package ch.cyberduck.core.box;

/*
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.api.UsersApi;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.BufferWriteFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Collections;

public class BoxSession extends HttpSession<CloseableHttpClient> {

    private final BoxFileidProvider fileid = new BoxFileidProvider(this);

    private OAuth2RequestInterceptor authorizationService;

    public BoxSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public CloseableHttpClient connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws ConnectionCanceledException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(configuration.build(), host, prompt)
                .withRedirectUri(host.getProtocol().getOAuthRedirectUrl());
        configuration.addInterceptorLast(authorizationService);
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        return configuration.build();
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            final Credentials credentials = authorizationService.validate();
            credentials.setUsername(new UsersApi(new BoxApiClient(client)).getUsersMe(Collections.emptyList()).getLogin());
        }
        catch(ApiException e) {
            throw new BoxExceptionMappingService(fileid).map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.close();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            fileid.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == FileIdProvider.class) {
            return (T) fileid;
        }
        if(type == Upload.class) {
            return (T) new BoxThresholdUploadService(this, fileid, registry);
        }
        if(type == Write.class) {
            return (T) new BoxWriteFeature(this, fileid);
        }
        if(type == Touch.class) {
            return (T) new BoxTouchFeature(this, fileid);
        }
        if(type == MultipartWrite.class) {
            return (T) new BufferWriteFeature(this);
        }
        if(type == ListService.class) {
            return (T) new BoxListService(this, fileid);
        }
        if(type == Read.class) {
            return (T) new BoxReadFeature(this, fileid);
        }
        if(type == Move.class) {
            return (T) new BoxMoveFeature(this, fileid);
        }
        if(type == Copy.class) {
            return (T) new BoxCopyFeature(this, fileid);
        }
        if(type == Directory.class) {
            return (T) new BoxDirectoryFeature(this, fileid);
        }
        if(type == Delete.class) {
            return (T) new BoxDeleteFeature(this, fileid);
        }
        if(type == AttributesFinder.class) {
            return (T) new BoxAttributesFinderFeature(this, fileid);
        }
        if(type == Share.class) {
            return (T) new BoxShareFeature(this, fileid);
        }
        return super._getFeature(type);
    }
}
