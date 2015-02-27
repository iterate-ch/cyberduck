package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFile;

import java.io.OutputStream;

/**
 * @version $Id$
 */
public class IRODSWriteFeature implements Write {

    private IRODSSession session;

    public IRODSWriteFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            return session.filesystem().getIRODSFileFactory().instanceIRODSFileOutputStream(
                    file.getAbsolute(), status.isAppend() ? DataObjInp.OpenFlags.WRITE : DataObjInp.OpenFlags.READ_WRITE);
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Uploading {0} failed", e, file);
        }
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.filesystem();
            final IRODSFile f = fs.getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
            if(f.exists()) {
                return new Append(length);
            }
            else {
                return Write.notfound;
            }
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean temporary() {
        return false;
    }
}
