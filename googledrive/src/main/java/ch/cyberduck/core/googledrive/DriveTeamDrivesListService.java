package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.drive.model.TeamDrive;
import com.google.api.services.drive.model.TeamDriveList;

public class DriveTeamDrivesListService implements ListService {
    private static final Logger log = LogManager.getLogger(DriveTeamDrivesListService.class);

    private final DriveSession session;
    private final DriveFileIdProvider fileid;
    private final int pagesize;

    public DriveTeamDrivesListService(final DriveSession session, final DriveFileIdProvider fileid) {
        this(session, fileid, 100);
    }

    public DriveTeamDrivesListService(final DriveSession session, final DriveFileIdProvider fileid, final int pagesize) {
        this.session = session;
        this.fileid = fileid;
        this.pagesize = pagesize;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            String page = null;
            do {
                final TeamDriveList list = session.getClient().teamdrives().list()
                    .setPageToken(page)
                    .setPageSize(pagesize)
                    .execute();
                for(TeamDrive f : list.getTeamDrives()) {
                    final Path child = new Path(directory, f.getName(), EnumSet.of(Path.Type.directory, Path.Type.volume),
                            new PathAttributes().withFileId(f.getId()));
                    children.add(child);
                }
                listener.chunk(directory, children);
                page = list.getNextPageToken();
                if(log.isDebugEnabled()) {
                    log.debug("Continue with next page token {}", page);
                }
            }
            while(page != null);
            return children;
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Listing directory failed", e, directory);
        }
    }
}
