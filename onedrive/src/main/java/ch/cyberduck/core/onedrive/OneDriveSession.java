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
import ch.cyberduck.core.URIEncoder;
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
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveJsonRequest;
import org.nuxeo.onedrive.client.OneDriveJsonResponse;
import org.nuxeo.onedrive.client.RequestExecutor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import com.eclipsesource.json.JsonObject;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.apache.ApacheHttpTransport;

public class OneDriveSession extends HttpSession<OneDriveAPI> {
    private static final Logger log = Logger.getLogger(OneDriveSession.class);

    private Credential credential;
    private OAuth2AuthorizationService authorizationService;
    private OneDriveCommonsHttpRequestExecutor httpRequestExecutor;

    public OneDriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected OneDriveAPI connect(final HostKeyCallback key) throws BackgroundException {
        final CloseableHttpClient client = builder.build(this).build();
        this.authorizationService = new OAuth2AuthorizationService(
                new ApacheHttpTransport(client),
                "https://login.live.com/oauth20_token.srf", "https://login.live.com/oauth20_authorize.srf",
                "372770ba-bb24-436b-bbd4-19bc86310c0e",
                "mJjWVkmfD9FVHNFTpbrdowv",
                Arrays.asList("onedrive.readwrite", "wl.offline_access"))
                .withRedirectUri("https://cyberduck.io/oauth");
        this.httpRequestExecutor = new OneDriveCommonsHttpRequestExecutor(client) {
            @Override
            protected void authenticate(final HttpRequestBase request) {
                request.addHeader("Authorization", String.format("Bearer %s", credential.getAccessToken()));
            }
        };
        return new OneDriveAPI() {
            @Override
            public RequestExecutor getExecutor() {
                return httpRequestExecutor;
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
                return "https://api.onedrive.com/v1.0";
            }

            @Override
            public String getEmailURL() {
                return "https://apis.live.net/v5.0/me";
            }
        };
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        final OAuth2AuthorizationService.Tokens tokens = authorizationService.find(keychain, host);
        this.login(keychain, prompt, cancel, cache, tokens);
    }

    private void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache, final OAuth2AuthorizationService.Tokens tokens) throws BackgroundException {
        credential = authorizationService.authorize(host, keychain, prompt, cancel, tokens);
        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        try {
            OneDriveFolder folder = OneDriveFolder.getRoot(client);
            OneDriveFolder.Metadata metadata = folder.getMetadata();
        }
        catch(IOException e) {
            try {
                throw new OneDriveExceptionMappingService().map((OneDriveAPIException)e);
            }
            catch(LoginFailureException f) {
                this.login(keychain, prompt, cancel, cache, OAuth2AuthorizationService.Tokens.EMPTY);
            }
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            httpRequestExecutor.shutdown();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new OneDriveListService(this).list(directory, listener);
    }

    public StringBuilder getBaseUrlStringBuilder() {
        // evaluating query
        StringBuilder builder = new StringBuilder();
        builder.append(getClient().getBaseURL());
        return builder;
    }

    public void resolveDriveQueryPath(final Path file, final StringBuilder builder, final PathContainerService pathContainerService) {
        builder.append("/drives"); // query single drive

        if(!file.isRoot()) {
            Path driveId = pathContainerService.getContainer(file); // using pathContainerService for retrieving current drive id
            builder.append(String.format("/%s", driveId.getName()));

            if(!pathContainerService.isContainer(file)) {
                // append path to item via pathContainerService with format :/path:
                builder.append(String.format("/root:/%s:", URIEncoder.encode(pathContainerService.getKey(file))));
            }
        }
    }

    public void resolveChildrenPath(final Path directory, final StringBuilder builder, final PathContainerService pathContainerService) {
        if(pathContainerService.isContainer(directory)) {
            builder.append("/root/children");
        }
        else if(!directory.isRoot()) {
            builder.append("/children");
        }
    }

    public URL getUrl(final StringBuilder builder) throws BackgroundException {
        try {
            return new URL(builder.toString());
        }
        catch(MalformedURLException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public JsonObject getSimpleResult(final URL url) throws BackgroundException {
        try {
            OneDriveJsonRequest request = new OneDriveJsonRequest(url, "GET");
            OneDriveJsonResponse response = request.sendRequest(client.getExecutor());
            return response.getContent();
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
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
