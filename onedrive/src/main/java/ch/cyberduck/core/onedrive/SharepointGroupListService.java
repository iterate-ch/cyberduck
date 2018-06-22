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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.GroupsIterator;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;
import org.nuxeo.onedrive.client.resources.GroupItem;

import java.util.EnumSet;

public class SharepointGroupListService implements ListService {
    private static final Logger log = Logger.getLogger(SharepointGroupListService.class);

    private final SharepointSession session;

    public SharepointGroupListService(final SharepointSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        final GroupsIterator groupsIterator = new GroupsIterator(session.getClient());

        while(groupsIterator.hasNext()) {
            final GroupItem.Metadata metadata;
            try {
                metadata = groupsIterator.next();
            }
            catch(OneDriveRuntimeException e) {
                log.warn(e.getMessage());
                continue;
            }
            final PathAttributes attributes = new PathAttributes();
            attributes.setVersionId(metadata.getId());
            children.add(new Path(directory, metadata.getDisplayName(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.placeholder), attributes));
            listener.chunk(directory, children);
        }

        return children;
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
