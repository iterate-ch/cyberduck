package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Search;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphBufferWriteFeature;
import ch.cyberduck.core.onedrive.features.GraphCopyFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.onedrive.features.GraphFindFeature;
import ch.cyberduck.core.onedrive.features.GraphMoveFeature;
import ch.cyberduck.core.onedrive.features.GraphQuotaFeature;
import ch.cyberduck.core.onedrive.features.GraphReadFeature;
import ch.cyberduck.core.onedrive.features.GraphSearchFeature;
import ch.cyberduck.core.onedrive.features.GraphTimestampFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveEmailAccount;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.RequestExecutor;
import org.nuxeo.onedrive.client.RequestHeader;

import java.io.IOException;
import java.util.Set;

public abstract class GraphSession extends HttpSession<OneDriveAPI> {
    private static final Logger log = Logger.getLogger(GraphSession.class);

    private OAuth2RequestInterceptor authorizationService;

    protected final GraphFileIdProvider fileIdProvider = new GraphFileIdProvider(this);

    protected GraphSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public OneDriveItem toItem(final Path currentPath) throws BackgroundException {
        return this.toItem(currentPath, true);
    }

    public abstract OneDriveItem toItem(final Path file, final boolean resolveLastItem) throws BackgroundException;

    public boolean isAccessible(final Path path) {
        return this.isAccessible(path, true);
    }

    public abstract boolean isAccessible(Path file, boolean container);

    public abstract Path getContainer(Path file);

    public OneDriveFile toFile(final Path file) throws BackgroundException {
        return this.toFile(file, true);
    }

    public OneDriveFile toFile(final Path file, final boolean resolveLastItem) throws BackgroundException {
        final OneDriveItem item = this.toItem(file, resolveLastItem);
        if(!(item instanceof OneDriveFile)) {
            throw new NotfoundException(String.format("%s is not a file.", file.getAbsolute()));
        }
        return (OneDriveFile) item;
    }

    public OneDriveFolder toFolder(final Path file) throws BackgroundException {
        return this.toFolder(file, true);
    }

    public OneDriveFolder toFolder(final Path file, final boolean resolveLastItem) throws BackgroundException {
        final OneDriveItem item = this.toItem(file, resolveLastItem);
        if(!(item instanceof OneDriveFolder)) {
            throw new NotfoundException(String.format("%s is not a folder.", file.getAbsolute()));
        }
        return (OneDriveFolder) item;
    }

    @Override
    protected OneDriveAPI connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        authorizationService = new OAuth2RequestInterceptor(configuration.build(), host.getProtocol()) {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                if(request.containsHeader(HttpHeaders.AUTHORIZATION)) {
                    super.process(request, context);
                }
            }
        }.withRedirectUri(host.getProtocol().getOAuthRedirectUrl());
        configuration.addInterceptorLast(authorizationService);
        configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(host, authorizationService, prompt));
        final RequestExecutor executor = new GraphCommonsHttpRequestExecutor(configuration.build()) {
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
                if(StringUtils.equals("graph.microsoft.com", host.getHostname())) {
                    return true;
                }
                else if(StringUtils.equals("graph.microsoft.de", host.getHostname())) {
                    return true;
                }
                return false;
            }

            @Override
            public String getBaseURL() {
                return String.format("%s://%s%s", host.getProtocol().getScheme(), host.getHostname(), host.getProtocol().getContext());
            }

            @Override
            public String getEmailURL() {
                return String.format("%s://%s%s", host.getProtocol().getScheme(), host.getHostname(), "/v1.0/me");
            }
        };
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        authorizationService.setTokens(authorizationService.authorize(host, prompt, cancel));
        try {
            final String account = OneDriveEmailAccount.getCurrentUserEmailAccount(client);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Authenticated as user %s", account));
            }
            host.getCredentials().setUsername(account);
        }
        catch(OneDriveAPIException e) {
            log.warn(String.format("Failure reading current user properties probably missing user.read scope. %s.", e.getMessage()));
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
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == IdProvider.class) {
            return (T) fileIdProvider;
        }
        if(type == AttributesFinder.class) {
            return (T) new GraphAttributesFinderFeature(this);
        }
        if(type == Directory.class) {
            return (T) new GraphDirectoryFeature(this);
        }
        if(type == Read.class) {
            return (T) new GraphReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new GraphWriteFeature(this);
        }
        if(type == MultipartWrite.class) {
            return (T) new GraphBufferWriteFeature(this);
        }
        if(type == Delete.class) {
            return (T) new GraphDeleteFeature(this);
        }
        if(type == Touch.class) {
            return (T) new GraphTouchFeature(this);
        }
        if(type == Move.class) {
            return (T) new GraphMoveFeature(this);
        }
        if(type == Copy.class) {
            return (T) new GraphCopyFeature(this);
        }
        if(type == Find.class) {
            return (T) new GraphFindFeature(this);
        }
        if(type == Search.class) {
            return (T) new GraphSearchFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new GraphTimestampFeature(this);
        }
        if(type == Quota.class) {
            return (T) new GraphQuotaFeature(this);
        }
        return super._getFeature(type);
    }
}
