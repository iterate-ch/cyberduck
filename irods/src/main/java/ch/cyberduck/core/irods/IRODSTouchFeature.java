package ch.cyberduck.core.irods;

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

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.pub.IRODSFileSystemAO;

public class IRODSTouchFeature implements Touch {

    private final IRODSSession session;

    public IRODSTouchFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.filesystem();
            fs.createFile(file.getAbsolute(),
                    DataObjInp.OpenFlags.WRITE_TRUNCATE, DataObjInp.DEFAULT_CREATE_MODE);
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }
}
