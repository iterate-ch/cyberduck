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
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Search;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveDrive;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.RequestExecutor;
import org.nuxeo.onedrive.client.RequestHeader;

import java.io.IOException;
import java.util.Set;

public class OneDriveSession extends HttpSession<OneDriveAPI> {

    private final PathContainerService containerService
            = new PathContainerService();

    private final OAuth2RequestInterceptor authorizationService = new OAuth2RequestInterceptor(builder.build(this).build(),
            host.getProtocol().getOAuthTokenUrl(),
            host.getProtocol().getOAuthAuthorizationUrl(),
            host.getProtocol().getClientId(),
            host.getProtocol().getClientSecret(),
            host.getProtocol().getScopes()) {
        @Override
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            if(request.containsHeader(HttpHeaders.AUTHORIZATION)) {
                super.process(request, context);
            }
        }
    }.withRedirectUri(PreferencesFactory.get().getProperty("onedrive.oauth.redirecturi"));

    public OneDriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    public OneDriveFile toFile(final Path file) {
        return new OneDriveFile(client, new OneDriveDrive(client, containerService.getContainer(file).getName()),
                URIEncoder.encode(containerService.getKey(file)));
    }

    public OneDriveFolder toFolder(final Path file) {
        if(file.isRoot()) {
            return OneDriveFolder.getRoot(client);
        }
        if(containerService.isContainer(file)) {
            return new OneDriveDrive(client, containerService.getContainer(file).getName()).getRoot();
        }
        return new OneDriveFolder(client, new OneDriveDrive(client, containerService.getContainer(file).getName()),
                URIEncoder.encode(containerService.getKey(file)));
    }

    @Override
    protected OneDriveAPI connect(final HostKeyCallback key) throws BackgroundException {
        final HttpClientBuilder configuration = builder.build(this);
        configuration.addInterceptorLast(authorizationService);
        final RequestExecutor executor = new OneDriveCommonsHttpRequestExecutor(configuration.build()) {
            @Override
            public void addAuthorizationHeader(final Set<RequestHeader> headers) {
                // Placeholder
                headers.add(new RequestHeader(HttpHeaders.AUTHORIZATION, "Bearer"));
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
                return StringUtils.equals("graph.microsoft.com", host.getHostname());
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
        authorizationService.setTokens(authorizationService.authorize(host, keychain, prompt, cancel));
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
        if(type == Move.class) {
            return (T) new OneDriveMoveFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new OneDriveAttributesFinderFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new OneDriveUrlProvider();
        }
        if(type == Home.class) {
            return (T) new OneDriveHomeFinderFeature(this);
        }
        if(type == Quota.class) {
            return (T) new OneDriveQuotaFeature(this);
        }
        if(type == Search.class) {
            return (T) new OneDriveSearchFeature(this);
        }
        return super._getFeature(type);
    }
}
