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

import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.PatchOperation;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.FileSystemInfo;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;

public class GraphTimestampFeature extends DefaultTimestampFeature {

    private final GraphSession session;
    private final GraphFileIdProvider fileid;

    public GraphTimestampFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        final PatchOperation patchOperation = new PatchOperation();
        final FileSystemInfo info = new FileSystemInfo();
        info.setCreatedDateTime(null != status.getCreated() ? Instant.ofEpochMilli(status.getCreated()).atOffset(ZoneOffset.UTC) : null);
        info.setLastModifiedDateTime(null != status.getModified() ? Instant.ofEpochMilli(status.getModified()).atOffset(ZoneOffset.UTC) : null);
        patchOperation.facet("fileSystemInfo", info);
        final DriveItem item = session.getItem(file);
        try {
            final DriveItem.Metadata metadata = Files.patch(item, patchOperation);
            status.setResponse(new GraphAttributesFinderFeature(session, fileid).toAttributes(metadata));
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
