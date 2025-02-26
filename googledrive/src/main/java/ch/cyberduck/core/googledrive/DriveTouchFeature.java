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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.text.MessageFormat;
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
            try {
                if(!new DriveAttributesFinderFeature(session, fileid).find(file).isTrashed()) {
                    throw new ConflictException(file.getAbsolute());
                }
            }
            catch(NotfoundException e) {
                // Ignore
            }
            final Drive.Files.Create insert = session.getClient().files().create(new File()
                    .setName(file.getName())
                    .setMimeType(status.getMime())
                    .setParents(Collections.singletonList(fileid.getFileId(file.getParent()))));
            final File execute = insert
                    .setFields(DriveAttributesFinderFeature.DEFAULT_FIELDS)
                    .setSupportsAllDrives(HostPreferencesFactory.get(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute();
            fileid.cache(file, execute.getId());
            return file.withAttributes(new DriveAttributesFinderFeature(session, fileid).toAttributes(execute));
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Cannot create {0}", e, file);
        }
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
        }
        if(new SimplePathPredicate(DriveHomeFinderService.SHARED_DRIVES_NAME).test(workdir)) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename)).withFile(workdir);
        }
    }
}
