package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.DefaultHttpRateLimiter;
import ch.cyberduck.core.http.ExecutionCountServiceUnavailableRetryStrategy;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.RateLimitingHttpRequestInterceptor;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.CustomDbxRawClientV2;
import com.dropbox.core.v2.common.PathRoot;
import com.dropbox.core.v2.users.DbxUserUsersRequests;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxSession extends HttpSession<CustomDbxRawClientV2> {
    private static final Logger log = LogManager.getLogger(DropboxSession.class);

    private final UseragentProvider useragent
            = new PreferencesUseragentProvider();

    private OAuth2RequestInterceptor authorizationService;

    private DropboxLockFeature locking;
    private DropboxShareFeature share;

    private PathRoot root = PathRoot.HOME;

    public DropboxSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected CustomDbxRawClientV2 connect(final ProxyFinder proxy, final HostKeyCallback callback, final LoginCallback prompt, final CancelCallback cancel) throws ConnectionCanceledException {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(configuration.build(), host, prompt)
                .withRedirectUri(host.getProtocol().getOAuthRedirectUrl())
                .withParameter("token_access_type", "offline");
        configuration.addInterceptorLast(authorizationService);
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                new ExecutionCountServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService))));
        if(new HostPreferences(host).getBoolean("dropbox.limit.requests.enable")) {
            configuration.addInterceptorLast(new RateLimitingHttpRequestInterceptor(new DefaultHttpRateLimiter(
                    new HostPreferences(host).getInteger("dropbox.limit.requests.second")
            )));
        }
        final CloseableHttpClient client = configuration.build();
        return new CustomDbxRawClientV2(DbxRequestConfig.newBuilder(useragent.get())
                .withAutoRetryDisabled()
                .withHttpRequestor(new DropboxCommonsHttpRequestExecutor(client)).build(),
                DbxHost.DEFAULT, null, null);
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            final Credentials credentials = authorizationService.validate();
            final FullAccount account = new DbxUserUsersRequests(client).getCurrentAccount();
            if(log.isDebugEnabled()) {
                log.debug("Authenticated as user {}", account);
            }
            credentials.setUsername(account.getEmail());
            switch(account.getAccountType()) {
                // The features listed below are only available to customers on Dropbox Professional, Standard, Advanced, and Enterprise.
                case PRO:
                case BUSINESS:
                    share = new DropboxPasswordShareFeature(this);
                    locking = new DropboxLockFeature(this);
                    break;
                default:
                    share = new DropboxShareFeature(this);
                    locking = null;
                    break;
            }
            // The Dropbox API Path Root is the folder that an API request operates relative to.
            final PathRoot root = PathRoot.root(account.getRootInfo().getRootNamespaceId());
            if(log.isDebugEnabled()) {
                log.debug("Set path root to {}", root);
            }
            this.root = root;
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            ((DropboxCommonsHttpRequestExecutor) client.getRequestConfig().getHttpRequestor()).close();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(Class<T> type) {
        if(type == ListService.class) {
            return (T) new DropboxListService(this);
        }
        if(type == Read.class) {
            return (T) new DropboxReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new DropboxWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DropboxUploadFeature(this, new DropboxWriteFeature(this));
        }
        if(type == Directory.class) {
            return (T) new DropboxDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new DropboxThresholdDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new DropboxMoveFeature(this);
        }
        if(type == Copy.class) {
            return (T) new DropboxCopyFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new DropboxUrlProvider(this);
        }
        if(type == Share.class) {
            return (T) share;
        }
        if(type == Find.class) {
            return (T) new DropboxFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new DropboxAttributesFinderFeature(this);
        }
        if(type == Quota.class) {
            return (T) new DropboxQuotaFeature(this);
        }
        if(type == Touch.class) {
            return (T) new DropboxTouchFeature(this);
        }
        if(type == Search.class) {
            return (T) new DropboxSearchFeature(this);
        }
        if(type == Lock.class) {
            return (T) locking;
        }
        if(type == Versioning.class) {
            return (T) new DropboxVersioningFeature(this);
        }
        if(type == PathContainerService.class) {
            return (T) new DropboxPathContainerService(this);
        }
        return super._getFeature(type);
    }

    public CustomDbxRawClientV2 getClient(final Path file) {
        return client.withPathRoot(root);
    }
}
