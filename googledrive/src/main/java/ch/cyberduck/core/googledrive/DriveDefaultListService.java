package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

public class DriveDefaultListService extends AbstractDriveListService {

    private final DriveFileIdProvider fileid;

    public DriveDefaultListService(final DriveSession session, final DriveFileIdProvider fileid) {
        super(session, fileid);
        this.fileid = fileid;
    }

    public DriveDefaultListService(final DriveSession session, final DriveFileIdProvider fileid, final int pagesize) {
        super(session, fileid, pagesize);
        this.fileid = fileid;
    }

    protected String query(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return String.format("'%s' in parents", fileid.getFileId(directory));
    }
}
