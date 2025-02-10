package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;

import java.util.EnumSet;

import com.google.api.services.drive.model.File;

public class DriveSharedFolderListService extends AbstractDriveListService {

    public DriveSharedFolderListService(final DriveSession session, final DriveFileIdProvider fileid) {
        super(session, fileid);
    }

    public DriveSharedFolderListService(final DriveSession session, final DriveFileIdProvider fileid, final int pagesize) {
        super(session, fileid, pagesize);
    }

    @Override
    protected String query(final Path directory, final ListProgressListener listener) {
        return "sharedWithMe";
    }

    @Override
    protected EnumSet<Path.Type> toType(final File f) {
        final EnumSet<Path.Type> type = super.toType(f);
        type.add(Path.Type.shared);
        return type;
    }
}
