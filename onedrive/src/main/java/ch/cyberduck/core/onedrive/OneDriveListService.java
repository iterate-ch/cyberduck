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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.features.onedrive.SharedWithMeListService;

import java.util.EnumSet;

public class OneDriveListService implements ListService {
    private static final String MYFILES_ID = "MYFILES_NAME";
    public static final Path MYFILES_NAME = new Path("/My Files", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(MYFILES_ID));
    public static final SimplePathPredicate MYFILES_PREDICATE = new SimplePathPredicate(MYFILES_NAME);
    private static final String SHARED_ID = "SHARED_NAME";
    public static final Path SHARED_NAME = new Path("/Shared", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(SHARED_ID));
    public static final SimplePathPredicate SHARED_PREDICATE = new SimplePathPredicate(SHARED_NAME);
    private final GraphSession session;

    public OneDriveListService(final GraphSession session) {
        this.session = session;
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
        else if(SHARED_PREDICATE.test(directory)) {
            return new SharedWithMeListService(session).list(directory, listener);
        }
        else {
            return new GraphItemListService(session).list(directory, listener);
        }
    }
}
