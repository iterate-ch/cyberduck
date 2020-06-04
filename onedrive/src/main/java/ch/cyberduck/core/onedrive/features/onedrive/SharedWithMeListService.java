package ch.cyberduck.core.onedrive.features.onedrive;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.AbstractItemListService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;

import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.resources.User;

import java.util.Iterator;

public class SharedWithMeListService extends AbstractItemListService {
    private final GraphSession session;

    public SharedWithMeListService(final GraphSession session) {
        super(new GraphAttributesFinderFeature(session));
        this.session = session;
    }

    @Override
    protected Iterator<OneDriveItem.Metadata> getIterator(final Path directory) throws BackgroundException {
        return Files.getSharedWithMe(User.getCurrent(session.getClient()));
    }

    @Override
    protected Path toPath(final OneDriveItem.Metadata metadata, final Path directory) {
        final Path path = super.toPath(metadata, directory);
        path.getType().add(Path.Type.shared);
        return super.toPath(metadata, directory);
    }
}
