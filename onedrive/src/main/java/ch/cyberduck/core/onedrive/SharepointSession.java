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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.types.Drive;

import java.util.Deque;

import static ch.cyberduck.core.onedrive.SharepointListService.*;

public class SharepointSession extends AbstractSharepointSession {
    private static final Logger log = LogManager.getLogger(SharepointSession.class);

    public SharepointSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    @Override
    public boolean isSingleSite() {
        return false;
    }

    @Override
    protected Drive findDrive(final ContainerItem driveContainer) throws BackgroundException {
        final String driveId = fileid.getFileId(driveContainer.getContainerPath().get());
        final GraphSession.ContainerItem parentContainer = getContainer(driveContainer.getContainerPath().get().getParent());
        if(parentContainer.getCollectionPath().map(p -> SharepointListService.GROUPS_CONTAINER.equals(p.getName())).orElse(false)) {
            return new Drive(getGroup(parentContainer.getContainerPath().get()), driveId);
        }
        else if(parentContainer.getContainerPath().map(p -> SharepointListService.DEFAULT_SITE.equals(p.getName())).orElse(false)) {
            // Handles /Default-case, which is a site.
            return new Drive(getSite(parentContainer.getContainerPath().get()), driveId);
        }
        else {
            // finds:
            // Sites/<site name>
            final GraphSession.ContainerItem containerItem = getContainer(parentContainer.getContainerPath().get());
            if(containerItem.getCollectionPath().map(p -> SharepointListService.SITES_CONTAINER.equals(p.getName())).orElse(false)) {
                return new Drive(getSite(containerItem.getContainerPath().get()), driveId);
            }
            else {
                return null;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new SharepointListService(this, fileid);
        }
        return super._getFeature(type);
    }

    @Override
    public ContainerItem getContainer(final Path file) {
        Deque<Path> pathDeque = decompose(file);

        Path lastContainer = null;
        Path lastCollection = null;
        boolean exit = false, nextExit = false, exitEarly = false;

        while(!exit && pathDeque.size() > 0) {
            final Path current = pathDeque.pop();
            exit = nextExit;
            nextExit = exitEarly;

            switch(current.getName()) {
                case DRIVES_CONTAINER:
                    nextExit = true;
                case SITES_CONTAINER:
                    lastCollection = current;
                    break;

                case GROUPS_CONTAINER:
                    lastCollection = current;
                    exitEarly = true;
                    break;

                default:
                    lastContainer = current;
            }
        }

        return new ContainerItem(lastContainer, lastCollection, exit);
    }
}
