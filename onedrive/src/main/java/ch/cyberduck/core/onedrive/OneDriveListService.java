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

import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDriveItemIterator;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;

public class OneDriveListService implements ListService {

    private final OneDriveSession session;

    public OneDriveListService(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        OneDriveItemIterator iterator = null;
        try {
            iterator = new OneDriveItemIterator(session.getClient(), new URL(String.format("%s/drive/root:%s:/children", session.getClient().getBaseURL(), directory.getAbsolute())));
        }
        catch(MalformedURLException e) {
            throw new BackgroundException(e);
        }
        while(iterator.hasNext()) {
            final OneDriveItem.Metadata metadata;
            try {
                metadata = iterator.next();
            }
            catch(OneDriveRuntimeException e) {
                continue;
            }

            final EnumSet<AbstractPath.Type> type;
            if(metadata.isFile()) {
                type = EnumSet.of(Path.Type.file);
            }
            else if(metadata.isFolder()) {
                type = EnumSet.of(Path.Type.directory);
            }
            else {
                continue; // ignore !file && !folder
            }

            children.add(new Path(directory, metadata.getName(), type));
        }
        return children;
    }
}
