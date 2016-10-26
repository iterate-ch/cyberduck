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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class DriveDirectoryFeature implements Directory {

    private final DriveSession session;

    public DriveDirectoryFeature(DriveSession session) {
        this.session = session;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null, null);
    }

    @Override
    public void mkdir(final Path file, final String region, final TransferStatus status) throws BackgroundException {
        try {
            // Identified by the special folder MIME type application/vnd.google-apps.folder
            final Drive.Files.Create insert = session.getClient().files().create(new File()
                    .setName(file.getName())
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(Collections.singletonList(new DriveFileidProvider(session).getFileid(file.getParent()))));
            insert.execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}
