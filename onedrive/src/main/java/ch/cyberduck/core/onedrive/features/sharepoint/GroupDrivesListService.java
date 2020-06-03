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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.onedrive.AbstractDriveListService;
import ch.cyberduck.core.onedrive.GraphSession;

import org.nuxeo.onedrive.client.GroupDrivesIterator;
import org.nuxeo.onedrive.client.OneDriveDrive;
import org.nuxeo.onedrive.client.resources.GroupItem;

import java.util.Iterator;

public class GroupDrivesListService extends AbstractDriveListService {

    private final GraphSession session;
    private final IdProvider idProvider;

    public GroupDrivesListService(final GraphSession session, final IdProvider idProvider) {
        this.session = session;
        this.idProvider = idProvider;
    }

    @Override
    protected Iterator<OneDriveDrive.Metadata> getIterator(final Path directory) throws BackgroundException {
        final GroupItem group = new GroupItem(session.getClient(), idProvider.getFileid(directory, new DisabledListProgressListener()));
        return new GroupDrivesIterator(session.getClient(), group);
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        idProvider.withCache(cache);
        return this;
    }
}
