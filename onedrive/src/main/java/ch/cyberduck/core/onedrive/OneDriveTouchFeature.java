package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFile;

import java.io.IOException;

public class OneDriveTouchFeature implements Touch {

    private final OneDriveSession session;

    public OneDriveTouchFeature(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        if(file.isRoot() || file.getParent().isRoot()) {
            throw new BackgroundException("Cannot create file here", "Create file in container");
        }

        final OneDriveFile oneDriveFile = session.getFile(file);

        try {
            final OneDriveFile.Metadata fileMetadata = oneDriveFile.create(status.getMime());
            // TODO
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return file;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }

    @Override
    public Touch withWriter(final Write writer) {
        return this;
    }
}
