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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.drive.model.File;

import static com.google.api.client.util.Data.NULL_STRING;

public class DriveMetadataFeature implements Metadata {

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveMetadataFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Map<String, String> getDefault(final Path file) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            final String fileid = this.fileid.getFileId(file);
            final Map<String, String> properties = session.getClient().files().get(fileid).setFields("properties")
                    .setSupportsAllDrives(HostPreferencesFactory.get(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute().getProperties();
            if(null == properties) {
                return Collections.emptyMap();
            }
            return properties;
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final String fileid = this.fileid.getFileId(file);
            final File body = new File();
            final Map<String, String> properties = new HashMap<>(status.getMetadata());
            for(String key : file.attributes().getMetadata().keySet()) {
                if(!properties.containsKey(key)) {
                    // Entries with null values are cleared in update and copy requests
                    properties.put(key, NULL_STRING);
                }
            }
            body.setProperties(properties);
            status.setResponse(new DriveAttributesFinderFeature(session, this.fileid).toAttributes(
                    session.getClient().files().update(fileid, body).setFields("properties").
                            setSupportsAllDrives(HostPreferencesFactory.get(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute()
            ));
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Failure to write attributes of {0}", e, file);
        }
    }
}
