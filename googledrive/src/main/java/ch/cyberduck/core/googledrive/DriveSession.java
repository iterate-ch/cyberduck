package ch.cyberduck.core.googledrive;

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
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class DriveSession extends HttpSession<Drive> {
    private static final Logger log = Logger.getLogger(DriveSession.class);

    private HttpTransport transport;

    private final JsonFactory json = new GsonFactory();

    private final Preferences preferences
            = PreferencesFactory.get();

    private final UseragentProvider useragent
            = new PreferencesUseragentProvider();

    private Credential credential;

    private OAuth2AuthorizationService authorizationService;

    public DriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected Drive connect(final HostKeyCallback callback) throws BackgroundException {
        this.transport = new ApacheHttpTransport(builder.build(this).build());
        this.authorizationService = new OAuth2AuthorizationService(transport,
                GoogleOAuthConstants.TOKEN_SERVER_URL, GoogleOAuthConstants.AUTHORIZATION_SERVER_URL,
                preferences.getProperty("google.drive.client.id"),
                preferences.getProperty("google.drive.client.secret"),
                Collections.singletonList(DriveScopes.DRIVE))
                .withLegacyPrefix(host.getProtocol().getDescription());
        return new Drive.Builder(transport, json, new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                request.setSuppressUserAgentSuffix(true);
                // Add bearer token to request
                credential.initialize(request);
            }
        })
                .setApplicationName(useragent.get())
                .build();
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        credential = authorizationService.authorize(host, keychain, prompt);
        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        try {
            client.files().list().executeUsingHead();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            transport.shutdown();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            super.logout();
        }
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new DriveListService(this).list(directory, listener);
    }

    public Credential getTokens() {
        return credential;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(Class<T> type) {
        if(type == Read.class) {
            return (T) new DriveReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new DriveWriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new DriveUploadFeature(this);
        }
        if(type == Directory.class) {
            return (T) new DriveDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new DriveDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new DriveMoveFeature(this);
        }
        if(type == Copy.class) {
            return (T) new DriveCopyFeature(this);
        }
        if(type == Touch.class) {
            return (T) new DriveTouchFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new DriveUrlProvider(this);
        }
        if(type == Home.class) {
            return (T) new DriveHomeFinderService(this);
        }
        if(type == IdProvider.class) {
            return (T) new DriveFileidProvider(this);
        }
        return super.getFeature(type);
    }
}
