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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.oauth.OAuth2ErrorResponseInterceptor;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.onedrive.features.*;
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
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveDrive;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePackageItem;
import org.nuxeo.onedrive.client.OneDriveRemoteItem;
import org.nuxeo.onedrive.client.RequestExecutor;
import org.nuxeo.onedrive.client.RequestHeader;

import java.io.IOException;
import java.util.Set;

public class OneDriveSession extends GraphSession {
    private final Logger logger = Logger.getLogger(OneDriveSession.class);

    private final PathContainerService containerService
        = new PathContainerService();

    private final OneDriveFileIdProvider fileIdProvider = new OneDriveFileIdProvider(this);

    public OneDriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    /**
     * Resolves given path to OneDriveItem
     */
    @Override
    public OneDriveItem toItem(final Path currentPath, final boolean resolveLastItem) throws BackgroundException {
        final String versionId = fileIdProvider.getFileid(currentPath, new DisabledListProgressListener());
        if(StringUtils.isEmpty(versionId)) {
            throw new NotfoundException(String.format("Version ID for %s is empty", currentPath.getAbsolute()));
        }
        final String[] idParts = versionId.split("/");
        if(idParts.length == 1) {
            return new OneDriveDrive(getClient(), idParts[0]).getRoot();
        }
        else {
            final String driveId;
            final String itemId;
            if(idParts.length == 2 || !resolveLastItem) {
                driveId = idParts[0];
                itemId = idParts[1];
            }
            else if(idParts.length == 4) {
                driveId = idParts[2];
                itemId = idParts[3];
            }
            else {
                throw new NotfoundException(currentPath.getAbsolute());
            }
            final OneDriveDrive drive = new OneDriveDrive(getClient(), driveId);
            if(currentPath.getType().contains(Path.Type.file)) {
                return new OneDriveFile(getClient(), drive, itemId, OneDriveItem.ItemIdentifierType.Id);
            }
            else if(currentPath.getType().contains(Path.Type.directory)) {
                return new OneDriveFolder(getClient(), drive, itemId, OneDriveItem.ItemIdentifierType.Id);
            }
            else if(currentPath.getType().contains(Path.Type.placeholder)) {
                return new OneDrivePackageItem(getClient(), drive, itemId, OneDriveItem.ItemIdentifierType.Id);
            }
        }
        throw new NotfoundException(currentPath.getAbsolute());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new OneDriveListService(this, fileIdProvider);
        }
        if(type == IdProvider.class) {
            return (T) fileIdProvider;
        }
        if(type == Directory.class) {
            return (T) new GraphDirectoryFeature(this);
        }
        if(type == Read.class) {
            return (T) new OneDriveReadFeature(this);
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
            return (T) new OneDriveMoveFeature(this);
        }
        if(type == Copy.class) {
            return (T) new GraphCopyFeature(this);
        }
        if(type == Find.class) {
            return (T) new OneDriveFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new GraphAttributesFinderFeature(this);
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

    private class OneDriveItemWrapper {
        private boolean resolveLastItem;
        private OneDriveItem item;
        private OneDriveItem.Metadata itemMetadata;

        public OneDriveItem getItem() {
            return item;
        }

        public OneDriveItem.Metadata getItemMetadata() {
            return itemMetadata;
        }

        public boolean isDefined() {
            return null != item;
        }

        public boolean shouldResolveLastItem() {
            return resolveLastItem;
        }

        public OneDriveItemWrapper(boolean resolveLastItem) {
            this.resolveLastItem = resolveLastItem;
        }

        public void setItem(OneDriveItem item, OneDriveItem.Metadata itemMetadata) {
            this.item = item;
            this.itemMetadata = itemMetadata;
        }

        public void resolveItem() {
            if(item instanceof OneDriveRemoteItem) {
                itemMetadata = ((OneDriveRemoteItem.Metadata) itemMetadata).getRemoteItem();
                item = itemMetadata.getResource();
            }
        }
    }

    private class SearchResult {
        private final boolean foundChild;
        private final OneDriveItem.Metadata child;

        public boolean isFoundChild() {
            return foundChild;
        }

        public OneDriveItem.Metadata getChild() {
            return child;
        }

        public SearchResult(boolean foundChild, OneDriveItem.Metadata child) {
            this.foundChild = foundChild;
            this.child = child;
        }
    }
}
