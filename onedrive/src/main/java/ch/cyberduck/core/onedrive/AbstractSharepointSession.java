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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.onedrive.features.GraphLockFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.types.Drive;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.GroupItem;
import org.nuxeo.onedrive.client.types.Site;

public abstract class AbstractSharepointSession extends GraphSession {
    private static final Logger log = Logger.getLogger(SharepointSession.class);

    public AbstractSharepointSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public abstract boolean isSingleSite();

    public abstract Site getSite(final Path file) throws BackgroundException;

    public abstract GroupItem getGroup(final Path file) throws BackgroundException;

    @Override
    public DriveItem toItem(final Path file, final boolean resolveLastItem) throws BackgroundException {
        final String versionId = fileIdProvider.getFileid(file, new DisabledListProgressListener());
        if(StringUtils.isEmpty(versionId)) {
            throw new NotfoundException(String.format("Version ID for %s is empty", file.getAbsolute()));
        }
        final String[] idParts = versionId.split(String.valueOf(Path.DELIMITER));
        if(idParts.length == 1) {
            return new Drive(getClient(), idParts[0]).getRoot();
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
                throw new NotfoundException(file.getAbsolute());
            }
            final Drive drive = new Drive(getClient(), driveId);
            if(file.getType().contains(Path.Type.file)) {
                return new DriveItem(drive, itemId);
            }
            else if(file.getType().contains(Path.Type.directory)) {
                return new DriveItem(drive, itemId);
            }
            else if(file.getType().contains(Path.Type.placeholder)) {
                return new DriveItem(drive, itemId);
            }
        }
        throw new NotfoundException(file.getAbsolute());
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
        return !file.isRoot();
    }

    @Override
    public Path getContainer(final Path file) {
        return new SharepointContainerService().getContainer(file);
    }
}
