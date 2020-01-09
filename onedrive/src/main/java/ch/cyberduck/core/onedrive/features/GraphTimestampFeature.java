package ch.cyberduck.core.onedrive.features;

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
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePatchOperation;
import org.nuxeo.onedrive.client.facets.FileSystemInfoFacet;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;

public class GraphTimestampFeature extends DefaultTimestampFeature {
    private final GraphSession session;

    public GraphTimestampFeature(final GraphSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        final OneDrivePatchOperation patchOperation = new OneDrivePatchOperation();
        final FileSystemInfoFacet info = new FileSystemInfoFacet();
        info.setLastModifiedDateTime(Instant.ofEpochMilli(status.getTimestamp()).atOffset(ZoneOffset.UTC));
        patchOperation.facet("fileSystemInfo", info);
        final OneDriveItem item = session.toItem(file);
        try {
            item.patch(patchOperation);
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
