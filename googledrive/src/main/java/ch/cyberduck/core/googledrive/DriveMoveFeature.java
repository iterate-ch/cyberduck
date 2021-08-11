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
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.model.File;

public class DriveMoveFeature implements Move {

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    private Delete delete;

    public DriveMoveFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.delete = new DriveDeleteFeature(session, fileid);
        this.fileid = fileid;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            if(status.isExists()) {
                delete.delete(Collections.singletonMap(renamed, status), connectionCallback, callback);
            }
            final String id = fileid.getFileId(file, new DisabledListProgressListener());
            File result = null;
            if(!StringUtils.equals(file.getName(), renamed.getName())) {
                // Rename title
                final File properties = new File();
                properties.setName(renamed.getName());
                properties.setMimeType(status.getMime());
                result = session.getClient().files().update(id, properties)
                    .setFields(DriveAttributesFinderFeature.DEFAULT_FIELDS)
                    .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable"))
                    .execute();
            }
            if(!file.getParent().equals(renamed.getParent())) {
                // Retrieve the existing parents to remove
                final StringBuilder previousParents = new StringBuilder();
                final File reference = session.getClient().files().get(id)
                    .setFields("parents")
                    .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable"))
                    .execute();
                for(String parent : reference.getParents()) {
                    previousParents.append(parent).append(',');
                }
                // Move the file to the new folder
                result = session.getClient().files().update(id, null)
                    .setAddParents(fileid.getFileId(renamed.getParent(), new DisabledListProgressListener()))
                    .setRemoveParents(previousParents.toString())
                    .setFields(DriveAttributesFinderFeature.DEFAULT_FIELDS)
                    .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable"))
                    .execute();
            }
            fileid.cache(file, null);
            fileid.cache(renamed, id);
            return renamed.withAttributes(new DriveAttributesFinderFeature(session, fileid).toAttributes(result));
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Cannot rename {0}", e, file);
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
        return !source.getType().contains(Path.Type.placeholder);
    }
}
