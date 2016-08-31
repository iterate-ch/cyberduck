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
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.DisabledX509HostnameVerifier;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxRequestUtil;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.v2.DbxRawClientV2;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;

public class DropboxSession extends SSLSession<DbxRawClientV2> {
    private static final Logger log = Logger.getLogger(DropboxSession.class);

    private Preferences preferences
            = PreferencesFactory.get();

    private final UseragentProvider useragent
            = new PreferencesUseragentProvider();

    private final CustomTrustSSLProtocolSocketFactory sslSocketFactory
            = new CustomTrustSSLProtocolSocketFactory(trust, key);

    private final OAuth2AuthorizationService authorizationService = new OAuth2AuthorizationService(
            new NetHttpTransport(),
            "https://api.dropboxapi.com/1/oauth2/token",
            "https://www.dropbox.com/1/oauth2/authorize",
            PreferencesFactory.get().getProperty("dropbox.client.id"),
            PreferencesFactory.get().getProperty("dropbox.client.secret"),
            Collections.emptyList()).withRedirectUri("https://cyberduck.io/oauth");

    private Credential tokens;

    private final DbxRequestConfig config = new DbxRequestConfig(
            useragent.get(), Locale.getDefault().toString(), new StandardHttpRequestor(
            StandardHttpRequestor.Config.builder()
                    //.withProxy()
                    .withConnectTimeout(this.timeout(), TimeUnit.MILLISECONDS)
                    .build()) {
        @Override
        protected void configureConnection(final HttpsURLConnection conn) throws IOException {
            conn.setHostnameVerifier(new DisabledX509HostnameVerifier());
            conn.setSSLSocketFactory(sslSocketFactory);
            super.configureConnection(conn);
        }
    });

    public DropboxSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected DbxRawClientV2 connect(final HostKeyCallback callback) throws BackgroundException {
        return new DbxRawClientV2(config, DbxHost.DEFAULT) {
            @Override
            protected void addAuthHeaders(final List<HttpRequestor.Header> headers) {
                DbxRequestUtil.addAuthHeader(headers, tokens.getAccessToken());
            }
        };
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache)
            throws BackgroundException {
        tokens = authorizationService.authorize(host, keychain, prompt);
    }

    @Override
    public AttributedList<Path> list(Path directory, ListProgressListener listener) throws BackgroundException {
        return new DropboxListService(this).list(directory, listener);
    }

    public <T> T getFeature(Class<T> type) {
        if(type == Read.class) {
            return (T) new DropboxReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new DropboxWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DropboxUploadFeature(this);
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
        if(type == Attributes.class) {
            return (T) new DropboxAttributesFeature(this);
        }
        if(type == Quota.class) {
            return (T) new DropboxQuotaFeature(this);
        }
        return super.getFeature(type);
    }
}
