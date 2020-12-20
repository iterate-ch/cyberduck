package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.util.Iterator;

public class GraphItemListService extends AbstractItemListService {
    private static final Logger log = Logger.getLogger(GraphItemListService.class);

    private final GraphSession session;
    private final GraphAttributesFinderFeature attributes;

    public GraphItemListService(final GraphSession session) {
        super(new GraphAttributesFinderFeature(session));
        this.session = session;
        this.attributes = new GraphAttributesFinderFeature(session);
    }

    @Override
    protected Iterator<DriveItem.Metadata> getIterator(final Path directory) throws BackgroundException {
        final DriveItem folder = session.toFolder(directory);
        return Files.getFiles(folder, PreferencesFactory.get().getInteger("onedrive.listing.chunksize"));
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        attributes.withCache(cache);
        return this;
    }
}
