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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;

import java.util.EnumSet;

/**
 * Created by alive on 13.02.2017.
 */
public class OneDriveListService implements ListService {

    private final OneDriveSession session;

    public OneDriveListService(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        /*OneDriveFolder oneDriveFolder = new OneDriveFolder(session.getClient(), directory.getAbsolute());
        for(OneDriveItem.Metadata metadata : oneDriveFolder) {
            final EnumSet<AbstractPath.Type> type;
            if(metadata.isFile()) {
                type = EnumSet.of(Path.Type.file);
            }
            else if(metadata.isFolder()) {
                type = EnumSet.of(Path.Type.directory);
            }
            else {
                type = null;
            }

            final Path child = new Path(directory, metadata.getName(), type, properties);
            children.add(child);
        }*/

        return children;
    }
}
