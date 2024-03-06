package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.UserAgentHttpRequestInitializer;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.storage.Storage;

public class GoogleStorageSession extends HttpSession<Storage> {

    private final PreferencesReader preferences
            = new HostPreferences(host);

    private ApacheHttpTransport transport;
    private OAuth2RequestInterceptor authorizationService;

    private final Versioning versioning =
            preferences.getBoolean("s3.versioning.enable") ? new GoogleStorageVersioningFeature(this) : null;

    public GoogleStorageSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected Storage connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws ConnectionCanceledException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(builder.build(ProxyFactory.get().find(host.getProtocol().getOAuthAuthorizationUrl()), this, prompt).build(), host, prompt)
                .withRedirectUri(host.getProtocol().getOAuthRedirectUrl());
        configuration.addInterceptorLast(authorizationService);
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        transport = new ApacheHttpTransport(configuration.build());
        final UseragentProvider ua = new PreferencesUseragentProvider();
        return new Storage.Builder(transport, new GsonFactory(), new UserAgentHttpRequestInitializer(ua))
                .setApplicationName(ua.get())
                .build();
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        authorizationService.validate();
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            transport.shutdown();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public HttpClient getHttpClient() {
        return transport.getHttpClient();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new GoogleStorageListService(this);
        }
        if(type == Touch.class) {
            return (T) new GoogleStorageTouchFeature(this);
        }
        if(type == Read.class) {
            return (T) new GoogleStorageReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new GoogleStorageWriteFeature(this);
        }
        if(type == Find.class) {
            return (T) new GoogleStorageFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new GoogleStorageAttributesFinderFeature(this);
        }
        if(type == AclPermission.class) {
            return (T) new GoogleStorageAccessControlListFeature(this);
        }
        if(type == Delete.class) {
            return (T) new GoogleStorageDeleteFeature(this);
        }
        if(type == Directory.class) {
            return (T) new GoogleStorageDirectoryFeature(this);
        }
        if(type == Move.class) {
            return (T) new GoogleStorageMoveFeature(this);
        }
        if(type == Headers.class) {
            return (T) new GoogleStorageMetadataFeature(this);
        }
        if(type == Metadata.class) {
            return (T) new GoogleStorageMetadataFeature(this);
        }
        if(type == Copy.class) {
            return (T) new GoogleStorageCopyFeature(this);
        }
        if(type == DistributionConfiguration.class) {
            return (T) new GoogleStorageWebsiteDistributionConfiguration(this);
        }
        if(type == Logging.class) {
            return (T) new GoogleStorageLoggingFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new GoogleStorageUrlProvider(this);
        }
        if(type == Share.class) {
            return (T) new GoogleStoragePublicUrlProvider(this);
        }
        if(type == Search.class) {
            return (T) new GoogleStorageSearchFeature(this);
        }
        if(type == Versioning.class) {
            return (T) versioning;
        }
        if(type == Location.class) {
            return (T) new GoogleStorageLocationFeature(this);
        }
        if(type == Lifecycle.class) {
            return (T) new GoogleStorageLifecycleFeature(this);
        }
        if(type == Redundancy.class) {
            return (T) new GoogleStorageStorageClassFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new GoogleStorageTimestampFeature(this);
        }
        return super._getFeature(type);
    }
}
