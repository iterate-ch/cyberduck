package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

public class DriveTrashedListService extends AbstractDriveListService {

    public DriveTrashedListService(final DriveSession session, final DriveFileIdProvider fileid) {
        super(session, fileid);
    }

    @Override
    protected String query(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return "trashed = true";
    }
}
