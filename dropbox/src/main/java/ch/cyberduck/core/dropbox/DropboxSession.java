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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UseragentProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxRequestUtil;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.v2.DbxRawClientV2;
import com.dropbox.core.v2.users.DbxUserUsersRequests;
import com.google.api.client.auth.oauth2.Credential;

public class DropboxSession extends HttpSession<DbxRawClientV2> {
    private static final Logger log = Logger.getLogger(DropboxSession.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private final UseragentProvider useragent
            = new PreferencesUseragentProvider();

    private Credential credentials;

    public DropboxSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected DbxRawClientV2 connect(final HostKeyCallback callback) throws BackgroundException {
        final CloseableHttpClient client = builder.build(this).build();
        return new DbxRawClientV2(DbxRequestConfig.newBuilder(useragent.get())
                .withAutoRetryDisabled()
                .withHttpRequestor(new DropboxCommonsHttpRequestExecutor(client)).build(), DbxHost.DEFAULT) {
            @Override
            protected void addAuthHeaders(final List<HttpRequestor.Header> headers) {
                if(null == credentials) {
                    log.warn("Missing authentication access token");
                    return;
                }
                DbxRequestUtil.addAuthHeader(headers, credentials.getAccessToken());
            }
        };
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache)
            throws BackgroundException {
        final OAuth2AuthorizationService authorizationService = new OAuth2AuthorizationService(((DropboxCommonsHttpRequestExecutor) client.getRequestConfig().getHttpRequestor()).getClient(),
                "https://api.dropboxapi.com/1/oauth2/token",
                "https://www.dropbox.com/1/oauth2/authorize",
                host.getProtocol().getClientId(),
                host.getProtocol().getClientSecret(),
                Collections.emptyList())
                .withRedirectUri(preferences.getProperty("dropbox.oauth.redirecturi"));
        final OAuth2AuthorizationService.Tokens tokens = authorizationService.find(keychain, host);
        this.login(authorizationService, keychain, prompt, cancel, tokens);
    }

    private void login(final OAuth2AuthorizationService authorizationService, final HostPasswordStore keychain, final LoginCallback prompt,
                       final CancelCallback cancel, final OAuth2AuthorizationService.Tokens tokens) throws BackgroundException {
        credentials = authorizationService.authorize(host, keychain, prompt, cancel, tokens);
        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        try {
            new DbxUserUsersRequests(client).getCurrentAccount();
        }
        catch(DbxException e) {
            try {
                throw new DropboxExceptionMappingService().map(e);
            }
            catch(LoginFailureException f) {
                this.login(authorizationService, keychain, prompt, cancel, OAuth2AuthorizationService.Tokens.EMPTY);
            }
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
    public AttributedList<Path> list(Path directory, ListProgressListener listener) throws BackgroundException {
        return new DropboxListService(this).list(directory, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(Class<T> type) {
        if(type == Read.class) {
            return (T) new DropboxReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new DropboxWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DropboxUploadFeature(new DropboxWriteFeature(this));
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
        if(type == IdProvider.class) {
            return (T) new DropboxIdProvider(this);
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
            return (T) new DefaultTouchFeature(new DropboxUploadFeature(new DropboxWriteFeature(this)));
        }
        return super._getFeature(type);
    }
}
