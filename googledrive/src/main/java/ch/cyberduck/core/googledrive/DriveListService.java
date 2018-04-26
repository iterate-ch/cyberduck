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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

public class DriveListService implements ListService {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveListService(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            final AttributedList<Path> list = new AttributedList<>();
            list.add(DriveHomeFinderService.MYDRIVE_FOLDER);
            list.add(DriveHomeFinderService.SHARED_FOLDER_NAME);
            list.add(DriveHomeFinderService.TEAM_DRIVES_NAME);
            listener.chunk(directory, list);
            return list;
        }
        else {
            if(DriveHomeFinderService.SHARED_FOLDER_NAME.equals(directory)) {
                return new DriveSharedFolderListService(session).list(directory, listener);
            }
            if(DriveHomeFinderService.TEAM_DRIVES_NAME.equals(directory)) {
                return new DriveTeamDrivesListService(session).list(directory, listener);
            }
            return new DriveDefaultListService(session, fileid).list(directory, listener);
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        fileid.withCache(cache);
        return this;
    }
}
