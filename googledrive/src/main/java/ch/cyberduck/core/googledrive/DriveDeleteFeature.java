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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

public class DriveDeleteFeature implements Delete {
    private static final Logger log = LogManager.getLogger(DriveDeleteFeature.class);

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveDeleteFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path f : files.keySet()) {
            if(f.isPlaceholder()) {
                log.warn("Ignore placeholder {}", f);
                continue;
            }
            callback.delete(f);
            try {
                if(new SimplePathPredicate(DriveHomeFinderService.SHARED_DRIVES_NAME).test(f.getParent())) {
                    session.getClient().teamdrives().delete(fileid.getFileId(f)).execute();
                }
                else {
                    if(f.attributes().isDuplicate()) {
                        log.warn("Delete file {} already in trash", f);
                        // Permanently deletes a file version
                        session.getClient().revisions().delete(fileid.getFileId(f), f.attributes().getVersionId()).execute();
                    }
                    else {
                        session.getClient().files().delete(fileid.getFileId(f))
                                .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute();
                    }
                }
                fileid.cache(f, null);
            }
            catch(IOException e) {
                throw new DriveExceptionMappingService(fileid).map("Cannot delete {0}", e, f);
            }
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }
}
