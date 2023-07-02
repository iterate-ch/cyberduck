package ch.cyberduck.core.owncloud;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.nextcloud.NextcloudDeleteFeature;
import ch.cyberduck.core.nextcloud.NextcloudHomeFeature;
import ch.cyberduck.core.nextcloud.NextcloudListService;
import ch.cyberduck.core.nextcloud.NextcloudShareProvider;
import ch.cyberduck.core.nextcloud.NextcloudUrlProvider;
import ch.cyberduck.core.nextcloud.NextcloudWriteFeature;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.HttpClientBuilder;

import static ch.cyberduck.core.oauth.OAuth2AuthorizationService.CYBERDUCK_REDIRECT_URI;

public class OwncloudSession extends DAVSession {

    private OAuth2RequestInterceptor authorizationService;

    public OwncloudSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected HttpClientBuilder getConfiguration(final Proxy proxy, final LoginCallback prompt) {
        final HttpClientBuilder configuration = super.getConfiguration(proxy, prompt);
        if(host.getProtocol().isOAuthConfigurable()) {
            authorizationService = new OAuth2RequestInterceptor(configuration.build(), host)
                    .withFlowType(OAuth2AuthorizationService.FlowType.valueOf(host.getProtocol().getAuthorization()))
                    .withRedirectUri(CYBERDUCK_REDIRECT_URI);
            configuration.addInterceptorLast(authorizationService);
            configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService, prompt));
        }
        return configuration;
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        if(host.getProtocol().isOAuthConfigurable()) {
            authorizationService.authorize(host, prompt, cancel);
        }
        super.login(proxy, prompt, cancel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Home.class) {
            return (T) new DelegatingHomeFeature(new WorkdirHomeFeature(host), new DefaultPathHomeFeature(host), new NextcloudHomeFeature(host));
        }
        if(type == ListService.class) {
            return (T) new NextcloudListService(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new OwncloudAttributesFinderFeature(this);
        }
        if(type == Lock.class) {
            // https://github.com/nextcloud/server/issues/1308
            return null;
        }
        if(type == Upload.class) {
            return (T) new HttpUploadFeature(new NextcloudWriteFeature(this));
        }
        if(type == Write.class) {
            return (T) new NextcloudWriteFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new NextcloudUrlProvider(this);
        }
        if(type == PromptUrlProvider.class) {
            return (T) new NextcloudShareProvider(this);
        }
        if(type == Versioning.class) {
            return (T) new OwncloudVersioningFeature(this);
        }
        if(type == Delete.class) {
            return (T) new NextcloudDeleteFeature(this);
        }
        if(type == Read.class) {
            return (T) new OwncloudReadFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new OwncloudTimestampFeature(this);
        }
        return super._getFeature(type);
    }
}
