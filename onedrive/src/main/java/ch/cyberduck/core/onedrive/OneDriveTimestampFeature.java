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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultTimestampFeature;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePatchOperation;
import org.nuxeo.onedrive.client.facets.FileSystemInfoFacet;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;

public class OneDriveTimestampFeature extends DefaultTimestampFeature {
    private final OneDriveSession session;

    public OneDriveTimestampFeature(OneDriveSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final Long modified) throws BackgroundException {
        final OneDrivePatchOperation patch = new OneDrivePatchOperation();
        final FileSystemInfoFacet info = new FileSystemInfoFacet();
        info.setLastModifiedDateTime(Instant.ofEpochMilli(modified).atOffset(ZoneOffset.UTC));
        patch.facet("fileSystemInfo", info);

        final OneDriveItem item = session.toItem(file);
        if(null == item) {
            throw new NotfoundException(String.format("Did not find %s", file));
        }

        try {
            item.patch(patch);
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
