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
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class DriveTouchFeature implements Touch<VersionId> {

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveTouchFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final Drive.Files.Create insert = session.getClient().files().create(new File()
                .setName(file.getName())
                .setMimeType(status.getMime())
                .setParents(Collections.singletonList(fileid.getFileId(file.getParent()))));
            final File execute = insert
                    .setFields(DriveAttributesFinderFeature.DEFAULT_FIELDS)
                    .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute();
            fileid.cache(file, execute.getId());
            return file.withAttributes(new DriveAttributesFinderFeature(session, fileid).toAttributes(execute));
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Cannot create {0}", e, file);
        }
    }

    @Override
    public DriveTouchFeature withWriter(final Write<VersionId> writer) {
        return this;
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        if(workdir.isRoot()) {
            return false;
        }
        else if(DriveHomeFinderService.SHARED_DRIVES_NAME.equals(workdir)) {
            return false;
        }
        return true;
    }
}
