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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.google.api.services.drive.model.File;

public class DriveMetadataFeature implements Metadata {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveMetadataFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Map<String, String> getDefault(final Local local) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            final String fileid = this.fileid.getFileid(file, new DisabledListProgressListener());
            final Map<String, String> properties = session.getClient().files().get(fileid).setFields("properties")
                .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute().getProperties();
            if(null == properties) {
                return Collections.emptyMap();
            }
            return properties;
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final String fileid = this.fileid.getFileid(file, new DisabledListProgressListener());
            final File body = new File();
            body.setProperties(status.getMetadata());
            session.getClient().files().update(fileid, body).setFields("properties").
                setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
