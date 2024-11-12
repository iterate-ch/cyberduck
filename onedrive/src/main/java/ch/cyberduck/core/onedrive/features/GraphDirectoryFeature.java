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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.IOException;
import java.text.MessageFormat;

public class GraphDirectoryFeature implements Directory<Void> {

    private final GraphSession session;
    private final GraphAttributesFinderFeature attributes;
    private final GraphFileIdProvider fileid;

    public GraphDirectoryFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.attributes = new GraphAttributesFinderFeature(session, fileid);
        this.fileid = fileid;
    }

    @Override
    public Path mkdir(final Path directory, final TransferStatus status) throws BackgroundException {
        final DriveItem folder = session.getItem(directory.getParent());
        try {
            final DriveItem.Metadata metadata = Files.createFolder(folder, directory.getName());
            final PathAttributes attr = attributes.toAttributes(metadata);
            fileid.cache(directory, attr.getFileId());
            return directory.withAttributes(attr);
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map("Cannot create folder {0}", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, directory);
        }
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(!session.isAccessible(workdir)) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename)).withFile(workdir);
        }
    }
}
