package ch.cyberduck.core.irods;

import java.io.IOException;

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSException;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

public class IRODSFindFeature implements Find {

    private final IRODSSession session;

    public IRODSFindFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            final IRODSConnection conn = session.getClient();
            return IRODSFilesystem.exists(conn.getRcComm(), file.getAbsolute());
        }
        catch(IOException | IRODSException e) {
            throw new IRODSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        
    }
}
