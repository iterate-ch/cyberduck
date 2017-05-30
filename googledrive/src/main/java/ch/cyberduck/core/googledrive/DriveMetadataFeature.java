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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.google.api.services.drive.model.File;

public class DriveMetadataFeature implements Headers {

    private final DriveSession session;

    public DriveMetadataFeature(final DriveSession session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getDefault(final Local local) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            final String fileid = new DriveFileidProvider(session).getFileid(file);
            return session.getClient().files().get(fileid).setFields("properties").execute().getProperties();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        try {
            final String fileid = new DriveFileidProvider(session).getFileid(file);
            final File body = new File();
            body.setProperties(metadata);
            session.getClient().files().update(fileid, body).setFields("properties").execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
