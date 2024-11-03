package ch.cyberduck.core.onedrive.features.sharepoint;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.AbstractDriveListService;
import ch.cyberduck.core.onedrive.AbstractSharepointSession;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.Drives;
import org.nuxeo.onedrive.client.types.Drive;
import org.nuxeo.onedrive.client.types.Site;

import java.util.Iterator;

public class SiteDrivesListService extends AbstractDriveListService {
    private static final Logger log = LogManager.getLogger(SiteDrivesListService.class);

    private final AbstractSharepointSession session;

    public SiteDrivesListService(final AbstractSharepointSession session, final GraphFileIdProvider fileid) {
        super(fileid);
        this.session = session;
    }

    @Override
    protected Iterator<Drive.Metadata> getIterator(final Path directory) throws BackgroundException {
        final Site site = session.getSite(directory.getParent());
        if(log.isDebugEnabled()) {
            log.debug("Return drives for site {}", site);
        }
        return Drives.getDrives(site);
    }
}
