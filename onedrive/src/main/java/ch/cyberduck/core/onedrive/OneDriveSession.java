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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.onedrive.features.GraphLockFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.onedrive.client.ODataQuery;
import org.nuxeo.onedrive.client.types.BaseItem;
import org.nuxeo.onedrive.client.types.Drive;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.ItemReference;
import org.nuxeo.onedrive.client.types.User;

import java.io.IOException;
import java.util.Optional;

public class OneDriveSession extends GraphSession {

    public final static ContainerItem MYFILES = new ContainerItem(OneDriveListService.MYFILES_NAME, null, true);
    public final static ContainerItem SHAREDFILES = new ContainerItem(null, OneDriveListService.SHARED_NAME, false);

    public OneDriveSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public String getFileId(final DriveItem.Metadata metadata) {
        final ItemReference parent = metadata.getParentReference();
        if(metadata.getRemoteItem() != null) {
            final DriveItem.Metadata remoteMetadata = metadata.getRemoteItem();
            final ItemReference remoteParent = remoteMetadata.getParentReference();
            if(parent == null) {
                return String.join(String.valueOf(Path.DELIMITER),
                        remoteParent.getDriveId(), remoteMetadata.getId());
            }
            else {
                return String.join(String.valueOf(Path.DELIMITER),
                        parent.getDriveId(), metadata.getId(),
                        remoteParent.getDriveId(), remoteMetadata.getId());
            }
        }
        else {
            return String.join(String.valueOf(Path.DELIMITER), parent.getDriveId(), metadata.getId());
        }
    }

    @Override
    public DriveItem.Metadata getMetadata(final DriveItem item, ODataQuery query) throws IOException {
        if (query == null) {
            query = new ODataQuery();
        }
        query.select(BaseItem.Property.ParentReference, DriveItem.Property.RemoteItem);
        return super.getMetadata(item, query);
    }

    /**
     * Resolves given path to OneDriveItem
     */
    @Override
    public DriveItem getItem(final Path file, final boolean resolveLastItem) throws BackgroundException {
        if(new SimplePathPredicate(OneDriveListService.MYFILES_NAME).test(file)) {
            final User.Metadata user = this.getUser();
            // creationType can be non-assigned (Microsoft Account)
            // or null, Inviation, LocalAccount or EmailVerified.
            // noinspection OptionalAssignedToNull
            if(null == user || user.getCreationType() == null) {
                return new Drive(client).getRoot();
            }
            else {
                return new Drive(user.asDirectoryObject()).getRoot();
            }
        }
        final String id = fileid.getFileId(file);
        if(StringUtils.isEmpty(id)) {
            throw new NotfoundException(String.format("Version ID for %s is empty", file.getAbsolute()));
        }

        // recursively find items …

        final String[] idParts = id.split(String.valueOf(Path.DELIMITER));
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
        if(file.attributes().isDuplicate()) {
            return false;
        }
        final ContainerItem containerItem = this.getContainer(file);

        // Operations using container access:
        // touch, directory
        // copy, move: Only for target check
        // Operations not using container access:
        // copy, move: For source check.

        // For touch/directory:
        // Allow inside any directory that is either /My Files or a subfolder of /Shared.

        // For copy/move
        // Allow from a non-container item to a container-item.
        // e.g.
        // * /My Files/Folder/ToBeMoved to /My Files
        // * /Shared/Shared Folder/Nested/ToBeMoved to /Shared/Shared Folder/

        // Test for Container.
        if(containerItem.isDrive()) {
            // Is /My Files.
            // Tests whether container access is used, orelse deny access to /My Files.
            return container || !containerItem.getContainerPath().map(new SimplePathPredicate(file)::test).orElse(false);
        }
        else {
            // Check for /Shared-path
            // Catches modification of items in /Shared
            Optional<Boolean> predicate = containerItem.getCollectionPath().map(new SimplePathPredicate(file)::test);
            if(!container) {
                // Append condition to /Shared-path check for
                // If file parent is /Shared then return inaccessible below
                // Cannot modify items in /Shared/*, but /Shared/**/*
                // User must not be able to rename, move or copy first level of /Shared.
                predicate = predicate.map(o -> o || containerItem.getCollectionPath().map(new SimplePathPredicate(file.getParent())::test).get());
            }
            // Fallback to deny access for invalid paths (/Invalid).
            // Logic is upside down. Predicate determines whether to block access. Flip it.
            return !predicate.orElse(true);
        }
    }

    @Override
    public ContainerItem getContainer(final Path file) {
        if(new SimplePathPredicate(OneDriveListService.MYFILES_NAME).test(file) || file.isChild(OneDriveListService.MYFILES_NAME)) {
            return MYFILES;
        }
        if(new SimplePathPredicate(OneDriveListService.SHARED_NAME).test(file) || file.isChild(OneDriveListService.SHARED_NAME)) {
            return SHAREDFILES;
        }
        return ContainerItem.EMPTY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new OneDriveListService(this, fileid);
        }
        if(type == Lock.class) {
            final User.Metadata user = this.getUser();
            // this is a hack. Graph creationType can be present, but `null`, which is totally valid.
            // in order to determine whether this is a Microsoft or AAD account, we need to check for
            // a null-optional, not for non-present optional.
            //noinspection OptionalAssignedToNull
            if(null != user && null != user.getCreationType()) {
                return (T) new GraphLockFeature(this, fileid);
            }
        }
        return super._getFeature(type);
    }
}
