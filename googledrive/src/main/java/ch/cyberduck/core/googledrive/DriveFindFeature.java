package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.io.IOException;

import com.google.api.services.drive.model.File;

public class DriveFindFeature implements Find {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveFindFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        try {
            try {
                final File f = session.getClient().files().get(fileid.getFileid(file, new DisabledListProgressListener()))
                    .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
                return true;
            }
            catch(IOException e) {
                throw new DriveExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        fileid.withCache(cache);
        return this;
    }
}
