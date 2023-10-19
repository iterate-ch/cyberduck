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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.TeamDrive;

public class DriveDirectoryFeature implements Directory<VersionId> {

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveDirectoryFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            if(new SimplePathPredicate(DriveHomeFinderService.SHARED_DRIVES_NAME).test(folder.getParent())) {
                final TeamDrive execute = session.getClient().teamdrives().create(
                        new UUIDRandomStringService().random(), new TeamDrive().setName(folder.getName())
                ).execute();
                return folder.withAttributes(new PathAttributes(folder.attributes()).withFileId(execute.getId()));
            }
            else {
                try {
                    if(!new DriveAttributesFinderFeature(session, fileid).find(folder).isHidden()) {
                        throw new ConflictException(folder.getAbsolute());
                    }
                }
                catch(NotfoundException e) {
                    // Ignore
                }
                // Identified by the special folder MIME type application/vnd.google-apps.folder
                final Drive.Files.Create insert = session.getClient().files().create(new File()
                        .setName(folder.getName())
                        .setMimeType("application/vnd.google-apps.folder")
                        .setParents(Collections.singletonList(fileid.getFileId(folder.getParent()))));
                final File execute = insert
                        .setFields(DriveAttributesFinderFeature.DEFAULT_FIELDS)
                        .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute();
                fileid.cache(folder, execute.getId());
                return folder.withAttributes(new DriveAttributesFinderFeature(session, fileid).toAttributes(execute));
            }
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public DriveDirectoryFeature withWriter(final Write<VersionId> writer) {
        return this;
    }


    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        new DriveTouchFeature(session, fileid).preflight(workdir, filename);
    }
}
