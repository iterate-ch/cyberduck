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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.onedrive.AbstractDriveListService;
import ch.cyberduck.core.onedrive.AbstractSharepointSession;
import ch.cyberduck.core.onedrive.SharepointSession;

import org.nuxeo.onedrive.client.Drives;
import org.nuxeo.onedrive.client.types.Drive;

import java.util.Iterator;

public class SiteDrivesListService extends AbstractDriveListService {
    private final AbstractSharepointSession session;

    public SiteDrivesListService(final AbstractSharepointSession session) {
        this.session = session;
    }

    @Override
    protected Iterator<Drive.Metadata> getIterator(final Path directory) throws BackgroundException {
        return Drives.getDrives(session.getSite(directory.getParent()));
    }
}
