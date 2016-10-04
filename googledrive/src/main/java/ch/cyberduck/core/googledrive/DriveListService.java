package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveListService implements ListService {
    private static final Logger log = Logger.getLogger(DriveListService.class);

    private final DriveSession session;

    private final int pagesize;

    private final DriveAttributesFeature attributes;

    public DriveListService(final DriveSession session) {
        this(session, PreferencesFactory.get().getInteger("google.drive.list.limit"));
    }

    public DriveListService(final DriveSession session, final int pagesize) {
        this.session = session;
        this.pagesize = pagesize;
        this.attributes = new DriveAttributesFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            String page = null;
            do {
                final FileList list = session.getClient().files().list()
                        .setQ(String.format("'%s' in parents", new DriveFileidProvider(session).getFileid(directory)))
                        .setPageToken(page)
                        .setFields("nextPageToken, files")
                        .setPageSize(pagesize).execute();
                for(File f : list.getFiles()) {
                    final PathAttributes properties = attributes.toAttributes(f);
                    if(properties == null) {
                        continue;
                    }
                    final EnumSet<AbstractPath.Type> type = DriveAttributesFeature.DRIVE_FOLDER.equals(
                            f.getMimeType()) ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file);
                    final Path child = new Path(directory, PathNormalizer.name(f.getName()), type, properties);
                    children.add(child);
                }
                listener.chunk(directory, children);
                page = list.getNextPageToken();
            }
            while(page != null);
            return children;
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Listing directory failed", e, directory);
        }
    }
}
