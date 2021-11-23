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
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Search;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.IOException;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.CustomDbxRawClientV2;
import com.dropbox.core.v2.common.PathRoot;
import com.dropbox.core.v2.users.DbxUserUsersRequests;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxSession extends HttpSession<CustomDbxRawClientV2> {
    private static final Logger log = Logger.getLogger(DropboxSession.class);

    private final UseragentProvider useragent
        = new PreferencesUseragentProvider();

    private OAuth2RequestInterceptor authorizationService;
    private Lock<String> locking = null;

    public DropboxSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    protected CustomDbxRawClientV2 connect(final Proxy proxy, final HostKeyCallback callback, final LoginCallback prompt, final CancelCallback cancel) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(configuration.build(), host.getProtocol())
            .withRedirectUri(host.getProtocol().getOAuthRedirectUrl());
        configuration.addInterceptorLast(authorizationService);
        configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService, prompt));
        final CloseableHttpClient client = configuration.build();
        return new CustomDbxRawClientV2(DbxRequestConfig.newBuilder(useragent.get())
            .withAutoRetryDisabled()
            .withHttpRequestor(new DropboxCommonsHttpRequestExecutor(client)).build(),
            DbxHost.DEFAULT, null, null);
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        authorizationService.setTokens(authorizationService.authorize(host, prompt, cancel, OAuth2AuthorizationService.FlowType.AuthorizationCode));
        try {
            final FullAccount account = new DbxUserUsersRequests(client).getCurrentAccount();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Authenticated as user %s", account));
            }
            final Credentials credentials = host.getCredentials();
            credentials.setUsername(account.getEmail());
            credentials.setSaved(true);
            switch(account.getAccountType()) {
                case BUSINESS:
                    locking = new DropboxLockFeature(this);
            }
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
            return new HostPreferences(host).getBoolean("dropbox.business.enable") ?
                (T) new DropboxRootListService(this) : (T) new DropboxListService(this);
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
            return (T) new DropboxDeleteFeature(this);
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
        if(type == PromptUrlProvider.class) {
            return (T) new DropboxPasswordShareUrlProvider(this);
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
        return super._getFeature(type);
    }

    @Override
    public CustomDbxRawClientV2 getClient() {
        log.warn(String.format("Dropbox-API-Path-Root not set for client %s", client));
        return super.getClient();
    }

    public CustomDbxRawClientV2 getClient(final Path file) {
        return this.getClient(new DropboxPathContainerService(this).getNamespace(file));
    }

    /**
     * @param root The Dropbox-API-Path-Root header can be used to perform actions relative to a namespace without
     *             including the namespace as part of the path variable for every request.
     */
    protected CustomDbxRawClientV2 getClient(final PathRoot root) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set path root to %s", root));
        }
        // Syntax of using a namespace ID in the path parameter is only supported for namespaces that are mounted
        // under the root. That means it can't be used to access the team space itself. Must still set Dropbox-API-Path-Root header
        return client.withPathRoot(root);
    }
}
