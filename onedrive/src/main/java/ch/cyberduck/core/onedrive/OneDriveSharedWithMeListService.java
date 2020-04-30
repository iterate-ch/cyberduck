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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;

import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDriveItemIterator;
import org.nuxeo.onedrive.client.URLTemplate;

import java.util.Iterator;

public class OneDriveSharedWithMeListService extends AbstractItemListService {
    private static final URLTemplate SHAREDWITHME_LIST_URL = new URLTemplate("/drive/sharedWithMe");

    private final GraphSession session;

    public OneDriveSharedWithMeListService(final GraphSession session) {
        super(new GraphAttributesFinderFeature(session));
        this.session = session;
    }

    @Override
    protected Iterator<OneDriveItem.Metadata> getIterator(final Path directory) throws BackgroundException {
        return new OneDriveItemIterator(session.getClient(), SHAREDWITHME_LIST_URL.build(session.getClient().getBaseURL()));
    }

    @Override
    protected Path toPath(final OneDriveItem.Metadata metadata, final Path directory) {
        final Path path = super.toPath(metadata, directory);
        path.getType().add(Path.Type.shared);
        return super.toPath(metadata, directory);
    }
}
