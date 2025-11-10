package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.high_level.vfs.IRODSFilesystemException;

import java.io.IOException;

public class IRODSDirectoryFeature implements Directory<Void> {

    private final IRODSSession session;

    public IRODSDirectoryFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Write<Void> writer, final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            final IRODSConnection conn = session.getClient();
            IRODSFilesystem.createCollection(conn.getRcComm(), folder.getAbsolute());
            return folder;
        }
        catch(IRODSFilesystemException e) {
            throw new IRODSExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }
}
