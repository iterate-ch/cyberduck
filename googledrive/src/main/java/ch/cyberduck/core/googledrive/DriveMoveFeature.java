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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.model.File;

public class DriveMoveFeature implements Move {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    private Delete delete;

    public DriveMoveFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.delete = new DriveDeleteFeature(session, fileid);
        this.fileid = fileid;
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            if(status.isExists()) {
                delete.delete(Collections.singletonList(renamed), connectionCallback, callback);
            }
            final String fileid = this.fileid.getFileid(file, new DisabledListProgressListener());
            if(!StringUtils.equals(file.getName(), renamed.getName())) {
                // Rename title
                final File properties = new File();
                properties.setName(renamed.getName());
                properties.setMimeType(status.getMime());
                session.getClient().files().update(fileid, properties).
                    setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
            }
            // Retrieve the existing parents to remove
            final StringBuilder previousParents = new StringBuilder();
            final File reference = session.getClient().files().get(fileid)
                .setFields("parents")
                .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable"))
                .execute();
            for(String parent : reference.getParents()) {
                previousParents.append(parent);
                previousParents.append(',');
            }
            // Move the file to the new folder
            session.getClient().files().update(fileid, null)
                .setAddParents(this.fileid.getFileid(renamed.getParent(), new DisabledListProgressListener()))
                .setRemoveParents(previousParents.toString())
                .setFields("id, parents")
                .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable"))
                .execute();
            return new Path(renamed.getParent(), renamed.getName(), renamed.getType(),
                new PathAttributes(renamed.attributes()).withVersionId(fileid));
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(target.isRoot()) {
            return false;
        }
        if(DriveHomeFinderService.SHARED_FOLDER_NAME.equals(new PathContainerService().getContainer(target))) {
            return false;
        }
        return !source.getType().contains(Path.Type.placeholder);
    }
}
