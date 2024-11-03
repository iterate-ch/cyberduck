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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.Drives;
import org.nuxeo.onedrive.client.types.Drive;

import java.util.Iterator;

/**
 * List the available drives for a user (OneDrive) or SharePoint site (document libraries).
 */
public class GraphDrivesListService extends AbstractDriveListService {
    private static final Logger log = LogManager.getLogger(GraphDrivesListService.class);

    private final GraphSession session;

    public GraphDrivesListService(final GraphSession session, final GraphFileIdProvider fileid) {
        super(fileid);
        this.session = session;
    }

    @Override
    protected Iterator<Drive.Metadata> getIterator(final Path directory) {
        log.debug("Return drives for session {}", session);
        // In most cases, OneDrive and OneDrive for Business users will only have a single
        // drive available, the default drive. When using OneDrive API with a SharePoint team site,
        // this API returns the collection of document libraries created in the site.
        return Drives.getDrives(session.getClient());
    }
}
