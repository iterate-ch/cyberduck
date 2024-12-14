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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;

import com.google.api.services.drive.model.File;

public class DriveMoveFeature implements Move {
    private static final Logger log = LogManager.getLogger(DriveMoveFeature.class);

    private final DriveSession session;
    private final DriveFileIdProvider fileid;
    private final Delete delete;

    public DriveMoveFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.delete = new DriveTrashFeature(session, fileid);
        this.fileid = fileid;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            if(status.isExists()) {
                log.warn("Trash file {} to be replaced with {}", renamed, file);
                delete.delete(Collections.singletonMap(renamed, status), connectionCallback, callback);
            }
            final String id = fileid.getFileId(file);
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
            if(!new SimplePathPredicate(file.getParent()).test(renamed.getParent())) {
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
                    .setAddParents(fileid.getFileId(renamed.getParent()))
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
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(target.getParent().isRoot()) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
        if(source.isPlaceholder()) {
            // Disable for application/vnd.google-apps
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
    }
}
