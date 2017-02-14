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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

import java.util.EnumSet;

public class DriveDefaultListService extends AbstractDriveListService {

    private DriveSession session;

    public DriveDefaultListService(final DriveSession session) {
        super(session);
        this.session = session;
    }

    public DriveDefaultListService(final DriveSession session, final int pagesize) {
        super(session, pagesize);
        this.session = session;
    }

    protected String query(final Path directory) throws BackgroundException {
        return String.format("'%s' in parents", new DriveFileidProvider(session).getFileid(directory));
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = super.list(directory, listener);
        if(directory.isRoot()) {
            list.add(new Path(DriveHomeFinderService.SHARED_FOLDER_NAME, EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.volume)));
            listener.chunk(directory, list);
        }
        return list;
    }
}
