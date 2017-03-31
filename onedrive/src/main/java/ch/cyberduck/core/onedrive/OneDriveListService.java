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

import java.util.EnumSet;
import java.util.Iterator;

public class OneDriveListService implements ListService {
    private static final Logger log = Logger.getLogger(OneDriveListService.class);

    private final OneDriveSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    public OneDriveListService(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        try {
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
                final OneDriveDrive drive = new OneDriveDrive(session.getClient(), containerService.getContainer(directory).getName());
                final OneDriveFolder folder;
                if(containerService.isContainer(directory)) {
                    folder = drive.getRoot();
                }
                else {
                    folder = new OneDriveFolder(session.getClient(), drive, containerService.getKey(directory));
                }
                Iterator<OneDriveItem.Metadata> iterator = folder.iterator();
                while(iterator.hasNext()) {
                    final OneDriveItem.Metadata metadata;
                    try {
                        metadata = iterator.next();
                    }
                    catch(OneDriveRuntimeException e) {
                        log.warn(e);
                        continue;
                    }
                    final PathAttributes attributes = new PathAttributes();
                    children.add(new Path(directory, metadata.getName(),
                            metadata.isFolder() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), attributes));
                }
            }
        }
        catch(OneDriveRuntimeException e) { // this catches iterator.hasNext() which in return should fail fast
            throw new OneDriveExceptionMappingService().map(e.getCause());
        }
        return children;
    }
}
