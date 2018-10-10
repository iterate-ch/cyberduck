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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.onedrive.features.OneDriveHomeFinderFeature;
import ch.cyberduck.core.onedrive.features.OneDriveQuotaFeature;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.onedrive.client.OneDriveDrive;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePackageItem;

public class OneDriveSession extends GraphSession {

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
        final String[] idParts = versionId.split(String.valueOf(Path.DELIMITER));
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
    public boolean isAccessible(final Path path, final boolean container) {
        if (path.isRoot()){
            return false;
        }
        if (!container && path.getParent().isRoot()){
            return false;
        }
        return true;
    }

    @Override
    public Path getContainer(final Path path) {
        return new PathContainerService().getContainer(path);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new OneDriveListService(this);
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
        return super._getFeature(type);
    }
}
