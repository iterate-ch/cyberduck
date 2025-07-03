package ch.cyberduck.core.irods;

import java.io.IOException;

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;


public class IRODSTouchFeature implements Touch {

    private final IRODSSession session;

    public IRODSTouchFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {

            // Open and immediately close the file to create/truncate it
        	final IRODSConnection conn=session.getClient();
            try (IRODSDataObjectOutputStream out = new IRODSDataObjectOutputStream(conn.getRcComm(), file.getAbsolute(),
                    true /* truncate if exists */, false /* don't append */)) {
                // File is created or truncated by opening the stream
            }

            return file;
        }
        catch(IOException|IRODSException e) {
            throw new IRODSExceptionMappingService().map("Cannot create {0}", e, file);
        }
    }
}
