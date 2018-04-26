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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveDrive;
import org.nuxeo.onedrive.client.OneDriveDrivesIterator;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;

import java.util.EnumSet;

/**
 * List the available drives for a user (OneDrive) or SharePoint site (document libraries).
 */
public class OneDriveContainerListService implements RootListService {
    private static final Logger log = Logger.getLogger(OneDriveContainerListService.class);

    private final OneDriveSession session;

    public OneDriveContainerListService(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        // In most cases, OneDrive and OneDrive for Business users will only have a single
        // drive available, the default drive. When using OneDrive API with a SharePoint team site,
        // this API returns the collection of document libraries created in the site.
        final OneDriveDrivesIterator iter = new OneDriveDrivesIterator(session.getClient());
        while(iter.hasNext()) {
            final OneDriveDrive.Metadata metadata;
            try {
                metadata = iter.next();
            }
            catch(OneDriveRuntimeException e) {
                log.warn(e.getMessage());
                continue;
            }
            final PathAttributes attributes = new PathAttributes();
            attributes.setSize(metadata.getTotal());
            children.add(new Path(directory, metadata.getId(), EnumSet.of(Path.Type.directory, Path.Type.volume), attributes));
            listener.chunk(directory, children);
        }
        return children;
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
