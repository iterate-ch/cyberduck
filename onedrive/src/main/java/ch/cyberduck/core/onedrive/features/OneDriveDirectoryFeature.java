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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.onedrive.OneDriveExceptionMappingService;
import ch.cyberduck.core.onedrive.OneDriveSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFolder;

import java.io.IOException;

public class OneDriveDirectoryFeature implements Directory<Void> {

    private final OneDriveSession session;

    public OneDriveDirectoryFeature(OneDriveSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Path directory, final String region, final TransferStatus status) throws BackgroundException {
        final OneDriveFolder folder = session.toFolder(directory.getParent());
        try {
            final OneDriveFolder.Metadata metadata = folder.create(directory.getName());
            return new Path(directory.getParent(), directory.getName(), directory.getType(),
                new PathAttributes(directory.attributes()).withVersionId(
                    new OneDriveAttributesFinderFeature(session).toAttributes(metadata).getVersionId()
                ));
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Cannot create folder {0}", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, directory);
        }
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        return !workdir.isRoot();
    }

    @Override
    public Directory<Void> withWriter(final Write writer) {
        return this;
    }
}
