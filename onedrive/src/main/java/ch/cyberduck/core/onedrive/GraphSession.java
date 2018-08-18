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
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.RequestExecutor;
import org.nuxeo.onedrive.client.RequestHeader;

import java.io.IOException;
import java.util.Set;

public abstract class GraphSession extends HttpSession<OneDriveAPI> {
    private OAuth2RequestInterceptor authorizationService;

    protected GraphSession(final Host host, final ThreadLocalHostnameDelegatingTrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public OneDriveItem toItem(final Path currentPath) throws BackgroundException {
        return this.toItem(currentPath, true);
    }

    public abstract OneDriveItem toItem(final Path currentPath, final boolean resolveLastItem) throws BackgroundException;

    public OneDriveFile toFile(final Path currentPath) throws BackgroundException {
        return this.toFile(currentPath, true);
    }

    public OneDriveFile toFile(final Path currentPath, final boolean resolveLastItem) throws BackgroundException {
        final OneDriveItem item = this.toItem(currentPath, resolveLastItem);
        if(!(item instanceof OneDriveFile)) {
            throw new NotfoundException(String.format("%s is not a file.", currentPath.getAbsolute()));
        }
        return (OneDriveFile) item;
    }

    public OneDriveFolder toFolder(final Path currentPath) throws BackgroundException {
        return this.toFolder(currentPath, true);
    }

    public OneDriveFolder toFolder(final Path currentPath, final boolean resolveLastItem) throws BackgroundException {
        final OneDriveItem item = this.toItem(currentPath, resolveLastItem);
        if(!(item instanceof OneDriveFolder)) {
            throw new NotfoundException(String.format("%s is not a folder.", currentPath.getAbsolute()));
        }
        return (OneDriveFolder) item;
    }

    @Override
    protected OneDriveAPI connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt) {
        authorizationService = new OAuth2RequestInterceptor(builder.build(proxy, this, prompt).build(), host.getProtocol()) {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                if(request.containsHeader(HttpHeaders.AUTHORIZATION)) {
                    super.process(request, context);
                }
            }
        }.withRedirectUri(host.getProtocol().getOAuthRedirectUrl());
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);
        configuration.addInterceptorLast(authorizationService);
        configuration.setServiceUnavailableRetryStrategy(new OAuth2ErrorResponseInterceptor(authorizationService));
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
                if (StringUtils.equals("graph.microsoft.com", host.getHostname())) {
                    return true;
                }
                else if (StringUtils.equals("graph.microsoft.de", host.getHostname())) {
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
                return null;
            }
        };
    }

    @Override
    public void login(final Proxy proxy, final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
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
}
