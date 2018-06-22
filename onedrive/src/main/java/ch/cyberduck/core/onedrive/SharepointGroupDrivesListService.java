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
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.GroupDrivesIterator;
import org.nuxeo.onedrive.client.resources.GroupItem;

public class SharepointGroupDrivesListService extends AbstractDriveListService {
    private static final Logger log = Logger.getLogger(SharepointGroupDrivesListService.class);

    private final GraphSession session;

    public SharepointGroupDrivesListService(final GraphSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();

        final GroupItem group = new GroupItem(session.getClient(), directory.attributes().getVersionId());
        final GroupDrivesIterator iterator = new GroupDrivesIterator(session.getClient(), group);
        run(iterator, directory, children, listener);

        return children;
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
