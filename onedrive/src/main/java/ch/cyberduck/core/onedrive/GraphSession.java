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
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.onedrive.features.*;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.RequestExecutor;
import org.nuxeo.onedrive.client.RequestHeader;
import org.nuxeo.onedrive.client.Users;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.User;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public abstract class GraphSession extends HttpSession<OneDriveAPI> {
    private final static String API_VERSION = "v1.0";

    private static final Logger log = Logger.getLogger(GraphSession.class);

    private OAuth2RequestInterceptor authorizationService;

    private User.Metadata user;

    public User.Metadata getUser() {
        return user;
    }

    protected final GraphFileIdProvider fileIdProvider = new GraphFileIdProvider(this);

    protected GraphSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public DriveItem toItem(final Path currentPath) throws BackgroundException {
        return this.toItem(currentPath, true);
    }

    public abstract DriveItem toItem(final Path file, final boolean resolveLastItem) throws BackgroundException;

    public boolean isAccessible(final Path path) {
        return this.isAccessible(path, true);
    }

    public abstract boolean isAccessible(Path file, boolean container);

    public abstract ContainerItem getContainer(Path file);

    public DriveItem toFile(final Path file) throws BackgroundException {
        return this.toFile(file, true);
    }

    public DriveItem toFile(final Path file, final boolean resolveLastItem) throws BackgroundException {
        final DriveItem item = this.toItem(file, resolveLastItem);
        if(!(item instanceof DriveItem)) {
            throw new NotfoundException(String.format("%s is not a file.", file.getAbsolute()));
        }
        return (DriveItem) item;
    }

    public DriveItem toFolder(final Path file) throws BackgroundException {
        return this.toFolder(file, true);
    }

    public DriveItem toFolder(final Path file, final boolean resolveLastItem) throws BackgroundException {
        final DriveItem item = this.toItem(file, resolveLastItem);
        if(!(item instanceof DriveItem)) {
            throw new NotfoundException(String.format("%s is not a folder.", file.getAbsolute()));
        }
        return (DriveItem) item;
    }

    @Override
    protected OneDriveAPI connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt) throws HostParserException {
        final HttpClientBuilder configuration = builder.build(ProxyFactory.get().find(host.getProtocol().getOAuthAuthorizationUrl()), this, prompt);
        authorizationService = new OAuth2RequestInterceptor(configuration.build(), host.getProtocol()) {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                if(request.containsHeader(HttpHeaders.AUTHORIZATION)) {
                    super.process(request, context);
                }
            }
        }.withRedirectUri(host.getProtocol().getOAuthRedirectUrl())
            .withParameter("prompt", "select_account");
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
                final String hostname = host.getProtocol().getDefaultHostname();
                switch(hostname.toLowerCase()) {
                    case "graph.microsoft.com":
                    case "graph.microsoft.de":
                    case "microsoftgraph.chinacloudapi.cn":
                        return true;
                }
                return false;
            }

            @Override
            public String getBaseURL() {
                return String.format("%s://%s/%s", host.getProtocol().getScheme(), host.getProtocol().getDefaultHostname(), API_VERSION);
            }

            @Override
            public String getEmailURL() {
                return String.format("%s%s", getBaseURL(), "/me");
            }
        };
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        authorizationService.setTokens(authorizationService.authorize(host, prompt, cancel));
        try {
            user = Users.get(User.getCurrent(client), User.Select.CreationType, User.Select.UserPrincipalName);
            final String account = user.getUserPrincipalName();
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
            return (T) new GraphQuotaFeature(this, getFeature(Home.class));
        }
        if(type == PromptUrlProvider.class) {
            return (T) new GraphPromptUrlProvider(this);
        }
        return super._getFeature(type);
    }

    public static final class ContainerItem {
        public static final ContainerItem EMPTY = new ContainerItem(null, null, false);

        private final Path collectionPath;
        private final Path containerPath;
        private final boolean isDrive;

        public boolean isDrive() {
            return isDrive;
        }

        public Optional<Path> getCollectionPath() {
            return Optional.ofNullable(collectionPath);
        }

        public Optional<Path> getContainerPath() {
            return Optional.ofNullable(containerPath);
        }

        public boolean isDefined() {
            return collectionPath != null && containerPath != null;
        }

        public boolean isContainerInCollection() {
            if(!isDefined()) {
                return false;
            }

            return containerPath.isChild(collectionPath);
        }

        public boolean isCollectionInContainer() {
            if(!isDefined()) {
                return false;
            }

            return collectionPath.isChild(containerPath);
        }

        public ContainerItem(final Path containerPath, final Path collectionPath, final boolean isDrive) {
            this.containerPath = containerPath;
            this.collectionPath = collectionPath;
            this.isDrive = isDrive;
        }
    }
}
