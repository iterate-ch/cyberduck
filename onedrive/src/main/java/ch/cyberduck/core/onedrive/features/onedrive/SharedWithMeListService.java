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
import ch.cyberduck.core.onedrive.AbstractItemListService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.User;

import java.util.Iterator;

public class SharedWithMeListService extends AbstractItemListService {
    private static final Logger log = LogManager.getLogger(SharedWithMeListService.class);

    private final GraphSession session;

    public SharedWithMeListService(final GraphSession session, final GraphFileIdProvider fileid) {
        super(new GraphAttributesFinderFeature(session, fileid), fileid);
        this.session = session;
    }

    @Override
    protected Iterator<DriveItem.Metadata> getIterator(final Path directory) {
        final User user = User.getCurrent(session.getClient());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Return shared items for user %s", user));
        }
        return Files.getSharedWithMe(user);
    }
}
