package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;

import java.util.EnumSet;

public class SharepointListService implements ListService {

    private static final String DEFAULT_ID = "DEFAULT_NAME";
    private static final String GROUPS_ID = "GROUPS_NAME";

    public static final Path DEFAULT_NAME = new Path("/Default", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(DEFAULT_ID));
    public static final Path GROUPS_NAME = new Path("/Groups", EnumSet.of(Path.Type.volume, Path.Type.placeholder, Path.Type.directory), new PathAttributes().withVersionId(GROUPS_ID));

    private final SharepointSession session;
    private final IdProvider idProvider;

    public SharepointListService(final SharepointSession session, final IdProvider idProvider) {
        this.session = session;
        this.idProvider = idProvider;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            final AttributedList<Path> list = new AttributedList<>();
            list.add(DEFAULT_NAME);
            list.add(GROUPS_NAME);
            listener.chunk(directory, list);
            return list;
        }
        else {
            if(DEFAULT_NAME.equals(directory)) {
                return new GraphDrivesListService(session).list(directory, listener);
            }
            else if(GROUPS_NAME.equals(directory)) {
                return new SharepointGroupListService(session).list(directory, listener);
            }
            else if(GROUPS_NAME.equals(directory.getParent())) {
                return new SharepointGroupDrivesListService(session).list(directory, listener);
            }
            return new GraphItemListService(session).list(directory, listener);
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        idProvider.withCache(cache);
        return this;
    }
}
