package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.onedrive.features.onedrive.SharedWithMeListService;

import static ch.cyberduck.core.onedrive.OneDriveHomeFinderService.MYFILES_NAME;
import static ch.cyberduck.core.onedrive.OneDriveHomeFinderService.SHARED_NAME;

public class OneDriveListService implements ListService {

    private final GraphSession session;
    private final GraphFileIdProvider fileid;

    public OneDriveListService(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            final AttributedList<Path> list = new AttributedList<>();
            list.add(MYFILES_NAME);
            list.add(SHARED_NAME);
            listener.chunk(directory, list);
            return list;
        }
        else if(new SimplePathPredicate(SHARED_NAME).test(directory)) {
            return new SharedWithMeListService(session, fileid).list(directory, listener);
        }
        else {
            return new GraphItemListService(session, fileid).list(directory, listener);
        }
    }

}
