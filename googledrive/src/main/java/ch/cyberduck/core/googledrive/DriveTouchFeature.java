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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class DriveTouchFeature implements Touch<VersionId> {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveTouchFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final Drive.Files.Create insert = session.getClient().files().create(new File()
                    .setName(file.getName())
                    .setMimeType(status.getMime())
                .setParents(Collections.singletonList(fileid.getFileid(file.getParent(), new DisabledListProgressListener()))));
            final File execute = insert.
                setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
            return new Path(file.getParent(), file.getName(), file.getType(),
                    new PathAttributes(file.attributes()).withVersionId(execute.getId()));
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }

    @Override
    public DriveTouchFeature withWriter(final Write<VersionId> writer) {
        return this;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        if(workdir.isRoot()) {
            return false;
        }
        return true;
    }
}
