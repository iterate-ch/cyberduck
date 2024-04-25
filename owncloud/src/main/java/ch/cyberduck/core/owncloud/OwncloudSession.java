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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.dav.DAVClient;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.nextcloud.NextcloudDeleteFeature;
import ch.cyberduck.core.nextcloud.NextcloudListService;
import ch.cyberduck.core.nextcloud.NextcloudShareFeature;
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
import ch.cyberduck.core.tus.TusCapabilities;
import ch.cyberduck.core.tus.TusCapabilitiesResponseHandler;
import ch.cyberduck.core.tus.TusWriteFeature;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;

import static ch.cyberduck.core.tus.TusCapabilities.TUS_VERSION;

public class OwncloudSession extends DAVSession {
    private static final Logger log = LogManager.getLogger(OwncloudSession.class);

    private OAuth2RequestInterceptor authorizationService;
    private HttpUploadFeature upload;

    public OwncloudSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected DAVClient connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final DAVClient client = super.connect(proxy, key, prompt, cancel);
        final TusCapabilities capabilities = this.options(client);
        if(ArrayUtils.contains(capabilities.versions, TUS_VERSION) && capabilities.extensions.contains(TusCapabilities.Extension.creation)) {
            upload = new OcisUploadFeature(host, client.getClient(), new TusWriteFeature(capabilities, client.getClient()), capabilities);
        }
        else {
            upload = new HttpUploadFeature(new NextcloudWriteFeature(this));
        }
        return client;
    }

    private TusCapabilities options(final DAVClient client) throws BackgroundException {
        final HttpOptions options = new HttpOptions(URIEncoder.encode(
                new DelegatingHomeFeature(new DefaultPathHomeFeature(host)).find().getAbsolute()));
        try {
            final TusCapabilities capabilities = client.execute(options, new TusCapabilitiesResponseHandler());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Determined capabilities %s", capabilities));
            }
            return capabilities;
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    protected HttpClientBuilder getConfiguration(final Proxy proxy, final LoginCallback prompt) throws ConnectionCanceledException {
        final HttpClientBuilder configuration = super.getConfiguration(proxy, prompt);
        if(host.getProtocol().isOAuthConfigurable()) {
            authorizationService = new OAuth2RequestInterceptor(configuration.build(), host, prompt)
                    .withRedirectUri(host.getProtocol().getOAuthRedirectUrl());
            if(host.getProtocol().getAuthorization() != null) {
                authorizationService.withFlowType(OAuth2AuthorizationService.FlowType.valueOf(host.getProtocol().getAuthorization()));
            }
            configuration.addInterceptorLast(authorizationService);
            configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                    new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        }
        return configuration;
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        if(host.getProtocol().isOAuthConfigurable()) {
            final Credentials credentials = authorizationService.validate();
            final OAuthTokens oauth = credentials.getOauth();
            try {
                final String username = JWT.decode(oauth.getIdToken()).getClaim("preferred_username").asString();
                if(StringUtils.isNotBlank(username)) {
                    credentials.setUsername(username);
                }
            }
            catch(JWTDecodeException e) {
                log.warn(String.format("Failure %s decoding JWT %s", e, oauth.getIdToken()));
            }
        }
        super.login(proxy, prompt, cancel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Home.class) {
            return (T) new DelegatingHomeFeature(new WorkdirHomeFeature(host), new DefaultPathHomeFeature(host), new OwncloudHomeFeature(host));
        }
        if(type == ListService.class) {
            return (T) new NextcloudListService(this);
        }
        if(type == Directory.class) {
            return (T) new DAVDirectoryFeature(this, new OwncloudAttributesFinderFeature(this));
        }
        if(type == Touch.class) {
            return (T) new DAVTouchFeature(new NextcloudWriteFeature(this), new OwncloudAttributesFinderFeature(this));
        }
        if(type == AttributesFinder.class) {
            return (T) new OwncloudAttributesFinderFeature(this);
        }
        if(type == Upload.class) {
            return (T) upload;
        }
        if(type == Write.class) {
            return (T) new NextcloudWriteFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new NextcloudUrlProvider(this);
        }
        if(type == Share.class) {
            return (T) new NextcloudShareFeature(this);
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
