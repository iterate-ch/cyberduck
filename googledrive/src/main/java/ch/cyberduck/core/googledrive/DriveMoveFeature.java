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

import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import org.apache.commons.codec.binary.StringUtils;

import java.io.IOException;

import com.google.api.services.drive.model.File;

public class DriveMoveFeature implements Move {

    private final DriveSession session;

    private Delete delete;
    private ListService list;

    public DriveMoveFeature(DriveSession session) {
        this.session = session;
        this.delete = new DriveDeleteFeature(session);
        this.list = new DriveListService(session);
    }

    @Override
    public boolean isSupported(Path source, final Path target) {
        return true;
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

    @Override
    public Move withList(final ListService list) {
        this.list = list;
        return this;
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        try {
            final String fileid = new DriveFileidProvider(session).getFileid(file);
            if(!StringUtils.equals(file.getName(), renamed.getName())) {
                // Rename title
                final File properties = new File();
                properties.setName(renamed.getName());
                final File update = session.getClient().files().update(fileid, properties).execute();
            }
            // Retrieve the existing parents to remove
            final StringBuilder previousParents = new StringBuilder();
            final File reference = session.getClient().files().get(fileid)
                    .setFields("parents")
                    .execute();
            for(String parent : reference.getParents()) {
                previousParents.append(parent);
                previousParents.append(',');
            }
            // Move the file to the new folder
            session.getClient().files().update(fileid, null)
                    .setAddParents(new DriveFileidProvider(session).getFileid(renamed.getParent()))
                    .setRemoveParents(previousParents.toString())
                    .setFields("id, parents")
                    .execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }
}
