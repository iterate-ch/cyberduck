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
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveJsonRequest;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;

public class OneDriveDirectoryFeature implements Directory {
    private static final Logger log = Logger.getLogger(OneDriveDirectoryFeature.class);

    private final OneDriveSession session;

    public OneDriveDirectoryFeature(OneDriveSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Path directory, final String region, final TransferStatus status) throws BackgroundException {
        if(directory.isRoot() || directory.getParent().isRoot()) {
            throw new BackgroundException("Cannot create directory here", "Create directory in container");
        }

        try {
            final OneDriveFolder.Metadata createdFolder = session.getDirectory(directory.getParent()).create(directory.getName());
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return directory;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return !workdir.isRoot();
    }

    @Override
    public Directory withWriter(final Write writer) {
        return this;
    }
}
