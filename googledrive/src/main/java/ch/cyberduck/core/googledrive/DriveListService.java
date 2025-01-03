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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;

public class DriveListService implements ListService {

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveListService(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            final AttributedList<Path> list = new AttributedList<>();
            list.add(DriveHomeFinderService.MYDRIVE_FOLDER);
            list.add(DriveHomeFinderService.SHARED_FOLDER_NAME);
            list.add(DriveHomeFinderService.SHARED_DRIVES_NAME);
            list.add(DriveHomeFinderService.TRASH_FOLDER);
            listener.chunk(directory, list);
            return list;
        }
        else {
            if(new SimplePathPredicate(DriveHomeFinderService.SHARED_FOLDER_NAME).test(directory)) {
                return new DriveSharedFolderListService(session, fileid).list(directory, listener);
            }
            if(new SimplePathPredicate(DriveHomeFinderService.SHARED_DRIVES_NAME).test(directory)) {
                return new DriveTeamDrivesListService(session, fileid).list(directory, listener);
            }
            if(new SimplePathPredicate(DriveHomeFinderService.TRASH_FOLDER).test(directory)) {
                return new DriveTrashedListService(session, fileid).list(directory, listener);
            }
            return new DriveDefaultListService(session, fileid).list(directory, listener);
        }
    }

}
