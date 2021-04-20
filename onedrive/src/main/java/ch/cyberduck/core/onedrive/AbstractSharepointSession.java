package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.onedrive.features.GraphLockFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.Drive;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.GroupItem;
import org.nuxeo.onedrive.client.types.Site;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public abstract class AbstractSharepointSession extends GraphSession {
    private static final Logger log = Logger.getLogger(SharepointSession.class);

    public AbstractSharepointSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public abstract boolean isSingleSite();

    public abstract Site getSite(final Path file) throws BackgroundException;

    public abstract GroupItem getGroup(final Path file) throws BackgroundException;

    @Override
    public String getFileId(final DriveItem.Metadata metadata) {
        return metadata.getId();
    }

    @Override
    public DriveItem getItem(final Path file, final boolean resolveLastItem) throws BackgroundException {
        final String versionId = fileIdProvider.getFileId(file, new DisabledListProgressListener());
        if(StringUtils.isEmpty(versionId)) {
            throw new NotfoundException(String.format("Version ID for %s is empty", file.getAbsolute()));
        }

        // Finds Sites/<Site Name>/Drives/<drive id>
        // Finds /Groups/<Groups Name>/<drive id>
        // Finds /Default/Drives/<drive id>
        // collection is: Drives
        // container is: Drives/<drive id>
        final GraphSession.ContainerItem driveContainer = getContainer(file);
        if(!driveContainer.isDrive()) {
            throw new NotfoundException(String.format("File %s is not in a drive.", file.getAbsolute()));
        }
        final Drive drive;
        final String driveId = fileIdProvider.getFileid(driveContainer.getContainerPath().get(), new DisabledListProgressListener());
        final GraphSession.ContainerItem parentContainer = getContainer(driveContainer.getContainerPath().get().getParent());
        if(parentContainer.getCollectionPath().map(p -> SharepointListService.GROUPS_CONTAINER.equals(p.getName())).orElse(false)) {
            drive = new Drive(getGroup(parentContainer.getContainerPath().get()), driveId);
        }
        else if(parentContainer.getContainerPath().map(p -> SharepointListService.DEFAULT_SITE.equals(p.getName())).orElse(false)) {
            // Handles /Default-case, which is a site.
            drive = new Drive(getSite(parentContainer.getContainerPath().get()), driveId);
        }
        else {
            // finds:
            // Sites/<site name>
            final GraphSession.ContainerItem containerItem = getContainer(parentContainer.getContainerPath().get());
            if (containerItem.getCollectionPath().map(p -> SharepointListService.SITES_CONTAINER.equals(p.getName())).orElse(false)) {
                drive = new Drive(getSite(containerItem.getContainerPath().get()), driveId);
            } else {
                throw new NotfoundException(String.format("File %s is not part of any drive.", file.getAbsolute()));
            }
        }

        final DriveItem ownItem;
        if(driveContainer.getContainerPath().map(file::equals).orElse(false)) {
            ownItem = drive.getRoot();
        }
        else {
            ownItem = new DriveItem(drive, versionId);
        }
        if(resolveLastItem) {
            try {
                final DriveItem.Metadata metadata = ownItem.getMetadata();
                final DriveItem.Metadata remoteMetadata = metadata.getRemoteItem();
                if(null != remoteMetadata) {
                    return (DriveItem) remoteMetadata.getItem();
                }
            }
            catch(OneDriveAPIException oneDriveAPIException) {
                throw new GraphExceptionMappingService().map(oneDriveAPIException);
            }
            catch(IOException ioException) {
                throw new DefaultIOExceptionMappingService().map(ioException);
            }
        }
        return ownItem;
    }

    @Override
    public <T> T _getFeature(final Class<T> type) {
        if(type == Lock.class) {
            return (T) new GraphLockFeature(this);
        }
        return super._getFeature(type);
    }

    @Override
    public boolean isAccessible(final Path file, final boolean container) {
        if(file.isRoot()) {
            return false;
        }

        final ContainerItem containerItem = getContainer(file);
        if(!containerItem.isDefined()) {
            return false;
        }
        return containerItem.isDrive() && (container || !containerItem.getContainerPath().map(file::equals).orElse(false));
    }

    protected Deque<Path> decompose(final Path file) {
        final Deque<Path> walk = new ArrayDeque<>();
        Path next = file;
        while(!next.isRoot()) {
            walk.push(next);
            next = next.getParent();
        }
        return walk;
    }
}
