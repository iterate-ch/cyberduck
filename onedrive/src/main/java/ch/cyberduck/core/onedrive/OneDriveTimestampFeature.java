package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.shared.DefaultTimestampFeature;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePatchOperation;
import org.nuxeo.onedrive.client.facets.FileSystemInfoFacet;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.sun.scenario.effect.Offset;

public class OneDriveTimestampFeature extends DefaultTimestampFeature {
    private final OneDriveSession session;

    public OneDriveTimestampFeature(OneDriveSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final Long modified) throws BackgroundException {
        final OneDrivePatchOperation patchOperation = new OneDrivePatchOperation();
        final FileSystemInfoFacet facet = new FileSystemInfoFacet();
        facet.setLastModifiedDateTime(Instant.ofEpochMilli(modified).atOffset(ZoneOffset.UTC));
        patchOperation.facet("fileSystemInfo", facet);

        try {
            session.toFile(file).patch(patchOperation);
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Cannot patch timestamp {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot patch timestamp {0}", e, file);
        }
    }
}
