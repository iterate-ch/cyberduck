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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.irods4j.high_level.io.IRODSDataObjectInputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectStream;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSApi.RcComm;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.io.InputStream;

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

            if(!IRODSFilesystem.exists(rcComm, logicalPath)) {
                throw new NotfoundException(logicalPath);
            }

            // Open input stream
            IRODSDataObjectInputStream in;
            String resource = session.getResource();
            if(resource.isEmpty()) {
                in = new IRODSDataObjectInputStream(rcComm, logicalPath);
            }
            else {
                in = new IRODSDataObjectInputStream(rcComm, logicalPath, resource);
            }

            // If resuming from offset, skip ahead
            if(status.isAppend() && status.getOffset() > 0) {
                long totalOffset = status.getOffset();
                while(totalOffset > 0) {
                    if(totalOffset >= Integer.MAX_VALUE) {
                        totalOffset -= Integer.MAX_VALUE;
                        in.seek(Integer.MAX_VALUE, IRODSDataObjectStream.SeekDirection.CURRENT);
                    }
                    else {
                        in.seek((int) totalOffset, IRODSDataObjectStream.SeekDirection.CURRENT);
                    }
                }
            }

            return in;
        }
        catch(IOException | IRODSException e) {
            throw new IRODSExceptionMappingService().map("Download {0} failed", e, file);
        }
    }
}
