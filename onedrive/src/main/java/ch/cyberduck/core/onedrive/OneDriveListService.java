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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveDrive;
import org.nuxeo.onedrive.client.OneDriveDrivesIterator;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDriveResource;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;

import java.net.URL;
import java.util.EnumSet;

public class OneDriveListService implements ListService {
    private static final Logger log = Logger.getLogger(OneDriveListService.class);

    private final OneDriveSession session;

    public OneDriveListService(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();

        // evaluating query
        StringBuilder builder = session.getBaseUrlStringBuilder();

        PathContainerService pathContainerService = new PathContainerService();
        session.resolveDriveQueryPath(directory, builder, pathContainerService);
        session.resolveChildrenPath(directory, builder, pathContainerService);

        final URL apiUrl = session.getUrl(builder);
        try {
            log.info(String.format("Querying OneDrive API with %s", apiUrl));
            if(directory.isRoot()) {
                final OneDriveDrivesIterator iter = new OneDriveDrivesIterator(session.getClient());
                while(iter.hasNext()) {
                    try {
                        final OneDriveResource.Metadata metadata = iter.next();
                        final PathAttributes attributes = new PathAttributes();
                        children.add(new Path(directory, metadata.getId(), EnumSet.of(Path.Type.directory, Path.Type.volume), attributes));
                    }
                    catch(OneDriveRuntimeException e) {
                        throw new OneDriveExceptionMappingService().map(e.getCause());
                    }
                }
            }
            else {
                final OneDriveDrive drive = new OneDriveDrive(session.getClient(), pathContainerService.getContainer(directory).getName());
                final OneDriveFolder folder;
                if(pathContainerService.isContainer(directory)) {
                    folder = drive.getRoot();
                }
                else {
                    folder = new OneDriveFolder(session.getClient(), drive, pathContainerService.getKey(directory));
                }
                for(final OneDriveItem.Metadata metadata : folder) {
                    try {
                        final PathAttributes attributes = new PathAttributes();
                        children.add(new Path(directory, metadata.getName(),
                                metadata.isFolder() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), attributes));
                    }
                    catch(OneDriveRuntimeException e) {
                        throw new OneDriveExceptionMappingService().map(e.getCause());
                    }
                }
            }
        }
        catch(OneDriveRuntimeException e) { // this catches iterator.hasNext() which in return should fail fast
            throw new OneDriveExceptionMappingService().map(e.getCause());
        }
        return children;
    }
}
