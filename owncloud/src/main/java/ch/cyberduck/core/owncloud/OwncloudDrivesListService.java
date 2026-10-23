package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.owncloud.model.Drives;

import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

/**
 * List project spaces
 */
public class OwncloudDrivesListService implements ListService {
    private static final Logger log = LogManager.getLogger(OwncloudDrivesListService.class);

    private final OwncloudSession session;

    public OwncloudDrivesListService(final OwncloudSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();
        for(Drives.Drive drive : this.drives()) {
            list.add(new Path(directory, drive.name, EnumSet.of(Path.Type.directory, Path.Type.volume),
                    new DefaultPathAttributes().setFileId(drive.root.id)));
        }
        listener.chunk(directory, list);
        return list;
    }

    /**
     * Retrieve spaces of user and update mapping to WebDAV root
     *
     * @return Project spaces
     */
    public List<Drives.Drive> drives() throws BackgroundException {
        try {
            final OwncloudDrivesRequest request = new OwncloudDrivesRequest(session.getHost());
            log.debug("Query spaces at {}", request);
            return session.mapper.withDrives(session.getClient().execute(request, new OwncloudDrivesResponseHandler()));
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
