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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Map;

import com.google.api.services.drive.model.File;

public class DriveDeleteFeature implements Delete {

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    private final Boolean trashing;

    public DriveDeleteFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getBoolean("googledrive.delete.trash"));
    }

    public DriveDeleteFeature(final DriveSession session, final DriveFileIdProvider fileid, final Boolean trashing) {
        this.session = session;
        this.fileid = fileid;
        this.trashing = trashing;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files.keySet()) {
            if(file.getType().contains(Path.Type.placeholder)) {
                continue;
            }
            callback.delete(file);
            try {
                if(DriveHomeFinderService.SHARED_DRIVES_NAME.equals(file.getParent())) {
                    session.getClient().teamdrives().delete(fileid.getFileId(file, new DisabledListProgressListener())).execute();
                }
                else {
                    if(!file.attributes().isDuplicate() && trashing) {
                        final File properties = new File();
                        properties.setTrashed(true);
                        session.getClient().files().update(fileid.getFileId(file, new DisabledListProgressListener()), properties)
                            .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute();
                    }
                    else {
                        session.getClient().files().delete(fileid.getFileId(file, new DisabledListProgressListener()))
                            .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute();
                    }
                }
                fileid.cache(file, null);
            }
            catch(IOException e) {
                throw new DriveExceptionMappingService(fileid).map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return !file.getType().contains(Path.Type.shared);
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
