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
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.onedrive.features.GraphLockFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.onedrive.client.types.Drive;
import org.nuxeo.onedrive.client.types.DriveItem;

import javax.swing.text.html.Option;
import java.util.Collections;

public class OneDriveSession extends GraphSession {

    public final static ContainerItem MYFILES = new ContainerItem(OneDriveListService.MYFILES_NAME, null, true);
    public final static ContainerItem SHAREDFILES = new ContainerItem(null, OneDriveListService.SHARED_NAME, false);

    public OneDriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    /**
     * Resolves given path to OneDriveItem
     */
    @Override
    public DriveItem toItem(final Path file, final boolean resolveLastItem) throws BackgroundException {
        if(file.equals(OneDriveListService.MYFILES_NAME)) {
            return new Drive(getUser().asDirectoryObject()).getRoot();
        }
        final String versionId = fileIdProvider.getFileid(file, new DisabledListProgressListener());
        if(StringUtils.isEmpty(versionId)) {
            throw new NotfoundException(String.format("Version ID for %s is empty", file.getAbsolute()));
        }
        final String[] idParts = versionId.split(String.valueOf(Path.DELIMITER));
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
            throw new NotfoundException(file.getAbsolute());
        }
        final Drive drive = new Drive(getClient(), driveId);
        return new DriveItem(drive, itemId);
    }

    @Override
    public boolean isAccessible(final Path file, final boolean container) {
        if(file.isRoot()) {
            return false;
        }

        final ContainerItem containerItem = getContainer(file);
        // Rename not possible in /Shared, items inside subfolder can be renamed
        return containerItem.isDrive() && (container || !containerItem.getContainerPath().map(file::equals).orElse(false)) ||
            !containerItem.isDrive() && !containerItem.getCollectionPath().map(o -> file.equals(o) || file.getParent().equals(o)).orElse(false);
    }

    @Override
    public ContainerItem getContainer(final Path file) {
        if(OneDriveListService.MYFILES_PREDICATE.test(file) || file.isChild(OneDriveListService.MYFILES_NAME)) {
            return MYFILES;
        }
        if(OneDriveListService.SHARED_PREDICATE.test(file) || file.isChild(OneDriveListService.SHARED_NAME)) {
            return SHAREDFILES;
        }
        return ContainerItem.EMPTY;
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
        if(type == Home.class) {
            return (T) new OneDriveHomeFinderService(this);
        }
        if(type == Lock.class) {
            // this is a hack. Graph creationType can be present, but `null`, which is totally valid.
            // in order to determine whether this is a Microsoft or AAD account, we need to check for
            // a null-optional, not for non-present optional.
            //noinspection OptionalAssignedToNull
            if(null != getUser() && null != getUser().getCreationType()) {
                return (T) new GraphLockFeature(this);
            }
        }
        return super._getFeature(type);
    }
}
