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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.onedrive.client.OneDriveDrive;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePackageItem;

public class SharepointSession extends GraphSession {

    public SharepointSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

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
        if(path.isRoot()) {
            return false;
        }
        if(path.isChild(SharepointListService.DEFAULT_NAME)) {
            // handles /Default_Name
            if(path == SharepointListService.DEFAULT_NAME) {
                return false;
            }
            // handles /Default_Name/Drive-ID
            if(!container && path.getParent() == SharepointListService.DEFAULT_NAME) {
                return false;
            }
        }
        else if(path.isChild(SharepointListService.GROUPS_NAME)) {
            // Handles /Groups_Name and /Groups_Name/Group
            if(path == SharepointListService.GROUPS_NAME || path.getParent() == SharepointListService.GROUPS_NAME) {
                return false;
            }
            // handles /Groups_Name/Group/Drive-ID
            if(!container && path.getParent().getParent() == SharepointListService.GROUPS_NAME) {
                return false;
            }
        }
        else {
            // Path is neither in /Default nor in /Groups
            // This should never happen.

            return false;
        }

        return true;
    }

    @Override
    public Path getContainer(final Path path) {
        if(path.isRoot()) {
            return path;
        }
        Path previous = path;
        Path parent = path.getParent();
        while (!parent.isRoot()) {
            if (parent.getParent() == SharepointListService.DEFAULT_NAME) {
                return parent;
            }
            else if (parent.getParent() == SharepointListService.GROUPS_NAME) {
                return previous;
            }
            previous = parent;
            parent = parent.getParent();
        }

        return path;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new SharepointListService(this, this.getFeature(IdProvider.class));
        }
        return super._getFeature(type);
    }
}
