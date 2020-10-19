package ch.cyberduck.core.onedrive.features.sharepoint;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.onedrive.AbstractListService;
import ch.cyberduck.core.onedrive.SharepointSession;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.Groups;
import org.nuxeo.onedrive.client.types.GroupItem;
import org.nuxeo.onedrive.client.types.User;

import java.util.EnumSet;
import java.util.Iterator;

public class GroupListService extends AbstractListService<GroupItem.Metadata> {
    private static final Logger log = Logger.getLogger(GroupListService.class);

    private final SharepointSession session;

    public GroupListService(final SharepointSession session) {
        this.session = session;
    }

    @Override
    protected Iterator<GroupItem.Metadata> getIterator(final Path directory) {
        return Groups.getMemberOfGroups(User.getCurrent(session.getClient()));
    }

    @Override
    protected Path toPath(final GroupItem.Metadata metadata, final Path directory) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setVersionId(metadata.getId());
        return new Path(directory, metadata.getDisplayName(), EnumSet.of(Path.Type.directory, Path.Type.placeholder),
                attributes);
    }
}
