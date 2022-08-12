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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.ODataQuery;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.util.Iterator;

public class GraphItemListService extends AbstractItemListService {
    private static final Logger log = LogManager.getLogger(GraphItemListService.class);

    private final GraphSession session;

    public GraphItemListService(final GraphSession session, final GraphFileIdProvider fileid) {
        super(new GraphAttributesFinderFeature(session, fileid), fileid);
        this.session = session;
    }

    @Override
    protected Iterator<DriveItem.Metadata> getIterator(final Path directory) throws BackgroundException {
        final DriveItem folder = session.getItem(directory);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Return files for folder %s", folder));
        }
        // getQuery(null): return new ODataQuery with default set of parameters
        // require listing Publication/VersionId
        return Files.getFiles(folder, session.getQuery(null).top(new HostPreferences(session.getHost()).getInteger("onedrive.listing.chunksize")));
    }
}
