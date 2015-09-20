package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

/**
 * @version $Id:$
 */
public class DriveDirectoryFeature implements Directory {

    private DriveSession session;

    public DriveDirectoryFeature(DriveSession session) {
        this.session = session;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null);
    }

    @Override
    public void mkdir(final Path file, final String region) throws BackgroundException {
        try {
            // Identified by the special folder MIME type application/vnd.google-apps.folder
            final Drive.Files.Insert insert = session.getClient().files().insert(new File()
                    .setTitle(file.getName())
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(Collections.singletonList(new ParentReference()
                            .setIsRoot(file.getParent().isRoot())
                            .setId(file.getParent().attributes().getVersionId()))));
            file.attributes().setVersionId(insert.execute().getId());
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}
