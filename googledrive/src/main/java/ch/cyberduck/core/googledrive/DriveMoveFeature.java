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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;

import java.io.IOException;

import com.google.api.services.drive.model.File;

public class DriveMoveFeature implements Move {

    private final DriveSession session;

    public DriveMoveFeature(final DriveSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !source.getType().contains(Path.Type.placeholder);
    }

    @Override
    public Move withDelete(final Delete delete) {
        return this;
    }

    @Override
    public void move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            final String fileid = new DriveFileidProvider(session).getFileid(file, new DisabledListProgressListener());
            if(!StringUtils.equals(file.getName(), renamed.getName())) {
                // Rename title
                final File properties = new File();
                properties.setName(renamed.getName());
                session.getClient().files().update(fileid, properties).execute();
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
                    .setAddParents(new DriveFileidProvider(session).getFileid(renamed.getParent(), new DisabledListProgressListener()))
                    .setRemoveParents(previousParents.toString())
                    .setFields("id, parents")
                    .execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }
}
