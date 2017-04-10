package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveDrive;
import org.nuxeo.onedrive.client.OneDriveExpand;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.RequestExecutor;

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;

public class OneDriveSession extends HttpSession<OneDriveAPI> {
    private static final Logger log = Logger.getLogger(OneDriveSession.class);

    private final PathContainerService containerService
            = new PathContainerService();

    private final Preferences preferences
            = PreferencesFactory.get();

    private Credential credential;

    public OneDriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    public OneDriveDrive getDrive(final Path file) {
        return new OneDriveDrive(client, containerService.getContainer(file).getName());
    }

    public OneDriveItem getItem(final Path file) {
        return new OneDriveItem(client, getDrive(file), containerService.getKey(file)) {
            @Override
            public Metadata getMetadata(final OneDriveExpand... expand) throws IOException {
                return null;
            }
        };
    }

    public OneDriveFile getFile(final Path file) {
        return new OneDriveFile(client, getDrive(file), containerService.getKey(file));
    }

    public OneDriveFolder getDirectory(final Path file) {
        final OneDriveDrive drive = getDrive(file);
        final String key = containerService.getKey(file);
        if(key == null) {
            return drive.getRoot();
        }
        return new OneDriveFolder(client, getDrive(file), key);
    }

    @Override
    protected OneDriveAPI connect(final HostKeyCallback key) throws BackgroundException {
        final CloseableHttpClient client = builder.build(this).build();
        final RequestExecutor executor = new OneDriveCommonsHttpRequestExecutor(client) {
            @Override
            protected void authenticate(final HttpRequestBase request) {
                request.addHeader("Authorization", String.format("Bearer %s", credential.getAccessToken()));
            }
        };
        return new OneDriveAPI() {
            @Override
            public RequestExecutor getExecutor() {
                return executor;
            }

            @Override
            public boolean isBusinessConnection() {
                return false;
            }

            @Override
            public boolean isGraphConnection() {
                return false;
            }

            @Override
            public String getBaseURL() {
                return String.format("%s://%s%s", host.getProtocol().getScheme(), host.getHostname(), host.getProtocol().getContext());
            }

            @Override
            public String getEmailURL() {
                return null;
            }
        };
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        final OAuth2AuthorizationService authorizationService = new OAuth2AuthorizationService(
                ((OneDriveCommonsHttpRequestExecutor) client.getExecutor()).getClient(),
                host.getProtocol().getOAuthTokenUrl(),
                host.getProtocol().getOAuthAuthorizationUrl(),
                host.getProtocol().getClientId(),
                host.getProtocol().getClientSecret(),
                host.getProtocol().getScopes())
                .withRedirectUri(preferences.getProperty("onedrive.oauth.redirecturi"));
        final OAuth2AuthorizationService.Tokens tokens = authorizationService.find(keychain, host);
        this.login(authorizationService, keychain, prompt, cancel, tokens);
    }

    protected void login(final OAuth2AuthorizationService authorizationService, final HostPasswordStore keychain, final LoginCallback prompt,
                         final CancelCallback cancel, final OAuth2AuthorizationService.Tokens tokens) throws BackgroundException {
        credential = authorizationService.authorize(host, keychain, prompt, cancel, tokens);
        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        try {
            OneDriveFolder folder = OneDriveFolder.getRoot(client);
            OneDriveFolder.Metadata metadata = folder.getMetadata();
        }
        catch(OneDriveAPIException e) {
            try {
                throw new OneDriveExceptionMappingService().map(e);
            }
            catch(LoginFailureException f) {
                this.login(authorizationService, keychain, prompt, cancel, OAuth2AuthorizationService.Tokens.EMPTY);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.getExecutor().close();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new OneDriveListService(this).list(directory, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Directory.class) {
            return (T) new OneDriveDirectoryFeature(this);
        }
        if(type == Read.class) {
            return (T) new OneDriveReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new OneDriveWriteFeature(this);
        }
        if(type == Delete.class) {
            return (T) new OneDriveDeleteFeature(this);
        }
        if(type == Touch.class) {
            return (T) new OneDriveTouchFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new OneDriveAttributesFinderFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new OneDriveUrlProvider();
        }
        return super._getFeature(type);
    }
}
