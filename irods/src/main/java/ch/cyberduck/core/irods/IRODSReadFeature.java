package ch.cyberduck.core.irods;

import java.io.IOException;
import java.io.InputStream;

import org.irods.irods4j.high_level.io.IRODSDataObjectInputStream;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSApi.RcComm;
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

public class IRODSReadFeature implements Read {

    private final IRODSSession session;

    public IRODSReadFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
     	
    		try {
            	final RcComm rcComm = session.getClient().getRcComm();
                final String logicalPath = file.getAbsolute(); // e.g., "/zone/home/user/file.txt"
                
                if (!IRODSFilesystem.exists(rcComm, logicalPath)) {
                    throw new NotfoundException(logicalPath);
                }
                
                // Open input stream
                InputStream in = new IRODSDataObjectInputStream(rcComm,logicalPath);

                // If resuming from offset, skip ahead
                if(status.isAppend() && status.getOffset() > 0) {
                    in.skip(status.getOffset());
                }

                return in;
            }
            catch(IOException | IRODSException e) {
                throw new IRODSExceptionMappingService().map("Download {0} failed", e, file);
            }
        
    }
}
