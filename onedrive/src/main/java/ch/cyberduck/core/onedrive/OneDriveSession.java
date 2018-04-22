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
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.onedrive.features.OneDriveAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.OneDriveBufferWriteFeature;
import ch.cyberduck.core.onedrive.features.OneDriveCopyFeature;
import ch.cyberduck.core.onedrive.features.OneDriveDeleteFeature;
import ch.cyberduck.core.onedrive.features.OneDriveDirectoryFeature;
import ch.cyberduck.core.onedrive.features.OneDriveFindFeature;
import ch.cyberduck.core.onedrive.features.OneDriveHomeFinderFeature;
import ch.cyberduck.core.onedrive.features.OneDriveMoveFeature;
import ch.cyberduck.core.onedrive.features.OneDriveQuotaFeature;
import ch.cyberduck.core.onedrive.features.OneDriveReadFeature;
import ch.cyberduck.core.onedrive.features.OneDriveSearchFeature;
import ch.cyberduck.core.onedrive.features.OneDriveTimestampFeature;
import ch.cyberduck.core.onedrive.features.OneDriveTouchFeature;
import ch.cyberduck.core.onedrive.features.OneDriveWriteFeature;
import ch.cyberduck.core.proxy.Proxy;
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
import org.nuxeo.onedrive.client.OneDriveDrivesIterator;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePackageItem;
import org.nuxeo.onedrive.client.OneDriveRemoteItem;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;
import org.nuxeo.onedrive.client.RequestExecutor;
import org.nuxeo.onedrive.client.RequestHeader;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class OneDriveSession extends GraphSession {

    private final PathContainerService containerService
        = new PathContainerService();

    private OAuth2RequestInterceptor authorizationService;

    public OneDriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    /**
     * Resolves given path to OneDriveResource
     */
    @Override
    public OneDriveItem toItem(final Path currentPath) throws BackgroundException {
        final Stack<String> parts = new Stack<String>();
        Path traverse = currentPath;
        while(!traverse.isRoot()) {
            parts.add(traverse.getName());

            traverse = traverse.getParent();
        }

        OneDriveDrive drive = null;
        OneDriveDrive.Metadata driveMetadata = null;
        OneDriveItem item = null;
        OneDriveItem.Metadata itemMetadata = null;

        while(!parts.empty()) {
            final String part = parts.pop();

            if(null == driveMetadata) {
                // external tracker for if a drive is found
                boolean foundDrive = false;
                // keeps track of latest known metadata (null if none or duplicate)
                OneDriveDrive.Metadata temporaryMetadata = null;

                final OneDriveDrivesIterator drivesIterator = new OneDriveDrivesIterator(getClient());
                // iterate through all drives
                while(drivesIterator.hasNext()) {
                    final OneDriveDrive.Metadata drivesIteratorMetadata = drivesIterator.next();

                    // compare ID, does not take Name into account (not applicable currently)
                    if(part.equals(drivesIteratorMetadata.getId())) {
                        // checks for first encounter
                        if(!foundDrive && null == temporaryMetadata) {
                            temporaryMetadata = drivesIteratorMetadata;
                            foundDrive = true;
                        }
                        else {
                            // resets temporaryMetadata to null for further usage
                            temporaryMetadata = null;
                        }
                    }
                }

                // temporaryMetadata may be null if there is no drive or a duplicate is found
                if(null == temporaryMetadata) {
                    if(foundDrive) {
                        return null;
                    }
                    else {
                        return null;
                    }
                }
                else {
                    // store drive
                    driveMetadata = temporaryMetadata;
                    drive = (OneDriveDrive) temporaryMetadata.getResource();
                    // continue on drive root
                    item = drive.getRoot();
                    itemMetadata = null;
                }
            }
            else {
                if(item instanceof OneDriveFolder) {
                    // external track for found child
                    boolean foundChild = false;
                    // temporary storage for found child (null if none or duplicate)
                    OneDriveItem.Metadata temporaryChild = null;

                    final OneDriveFolder folder = (OneDriveFolder) item;
                    // fast search for item
                    for(final OneDriveItem.Metadata childMetadata : folder.search(URIEncoder.encode(part))) {
                        // check name, do not take ID or anything else into account (not applicable)
                        // paths given here are always human readable
                        if(part.equals(childMetadata.getName())) {
                            if(!foundChild && null == temporaryChild) {
                                temporaryChild = childMetadata;
                                foundChild = true;
                            }
                            else {
                                temporaryChild = null;
                            }
                        }
                    }

                    // if nothing found try slower folder iteration
                    if(!foundChild) {
                        final Iterator<OneDriveItem.Metadata> oneDriveFolderIterator = folder.iterator();

                        while(oneDriveFolderIterator.hasNext()) {
                            try {
                                final OneDriveItem.Metadata childMetadata = oneDriveFolderIterator.next();

                                if(part.equals(childMetadata.getName())) {
                                    if(!foundChild && null == temporaryChild) {
                                        temporaryChild = childMetadata;
                                        foundChild = true;
                                    }
                                    else {
                                        temporaryChild = null;
                                    }
                                }
                            }
                            catch(OneDriveRuntimeException e) {
                                // silent ignore OneDriveRuntimeExceptions
                            }
                        }
                    }

                    if(null == temporaryChild) {
                        if(foundChild) {
                            return null;
                        }
                        else {
                            return null;
                        }
                    }
                    else {

                        if (temporaryChild instanceof OneDriveRemoteItem.Metadata) {
                            temporaryChild = ((OneDriveRemoteItem.Metadata)temporaryChild).getRemoteItem();
                        }

                        itemMetadata = temporaryChild;
                        item = temporaryChild.getResource();
                    }
                }
                else if(item instanceof OneDriveFile) {
                    return null; // cannot enumerate file
                }
                else if(item instanceof OneDrivePackageItem) {
                    return null; // Package Item not handled.
                }
                else {
                    return null; // unknown return
                }
            }
        }

        return item;
    }

    public OneDriveFile toFile(final Path file) {
        return new OneDriveFile(client, new OneDriveDrive(client, containerService.getContainer(file).getName()),
            URIEncoder.encode(containerService.getKey(file)), OneDriveItem.ItemIdentifierType.Path);
    }

    public OneDriveFolder toFolder(final Path file) {
        if(file.isRoot()) {
            return OneDriveDrive.getDefaultDrive(client).getRoot();
        }
        if(containerService.isContainer(file)) {
            return new OneDriveDrive(client, containerService.getContainer(file).getName()).getRoot();
        }
        return new OneDriveFolder(client, new OneDriveDrive(client, containerService.getContainer(file).getName()),
            URIEncoder.encode(containerService.getKey(file)), OneDriveItem.ItemIdentifierType.Path);
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
        if(type == MultipartWrite.class) {
            return (T) new OneDriveBufferWriteFeature(this);
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
        if(type == Copy.class) {
            return (T) new OneDriveCopyFeature(this);
        }
        if(type == Find.class) {
            return (T) new OneDriveFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new OneDriveAttributesFinderFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new OneDriveUrlProvider();
        }
        if(type == PromptUrlProvider.class) {
            return (T) new OneDriveSharingLinkUrlProvider(this);
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
        if(type == Timestamp.class) {
            return (T) new OneDriveTimestampFeature(this);
        }
        return super._getFeature(type);
    }
}
