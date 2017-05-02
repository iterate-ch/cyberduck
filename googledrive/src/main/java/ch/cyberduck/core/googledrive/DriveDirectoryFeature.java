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
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.serializer.PathAttributesDictionary;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class DriveDirectoryFeature implements Directory<Void> {

    private final DriveSession session;

    public DriveDirectoryFeature(DriveSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        try {
            // Identified by the special folder MIME type application/vnd.google-apps.folder
            final Drive.Files.Create insert = session.getClient().files().create(new File()
                    .setName(folder.getName())
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(Collections.singletonList(new DriveFileidProvider(session).getFileid(folder.getParent()))));
            final File execute = insert.execute();
            final Path p = new Path(folder.getParent(), folder.getName(), EnumSet.of(Path.Type.directory),
                    new PathAttributesDictionary().deserialize(folder.attributes().serialize(SerializerFactory.get())));
            p.attributes().setVersionId(execute.getId());
            return p;
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }

    @Override
    public DriveDirectoryFeature withWriter(final Write<Void> writer) {
        return this;
    }
}
