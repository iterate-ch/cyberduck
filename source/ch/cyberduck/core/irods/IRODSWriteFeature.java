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
            return session.getIrodsFileSystemAO().getIRODSFileFactory().instanceIRODSFileOutputStream(file.getAbsolute());
            // TODO research further
            /*if (status.isAppend()) {
                return session.getIrodsFileSystemAO().getIRODSFileFactory().instanceIRODSFileOutputStream(file.getAbsolute());
            } else {
                return session.getIrodsFileSystemAO().getIRODSFileFactory().instanceIRODSFileOutputStream(file.getAbsolute(), DataObjInp.OpenFlags.WRITE_FAIL_IF_EXISTS);
            }*/
        } catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Uploading {0} failed", e, file);
        }
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        try {
            IRODSFile irodsFile = session.getIrodsFileSystemAO().getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
            if (irodsFile.exists()) {
                return new Append(length);
                // TODO research further
                // TODO what is return Write.override; ?
            } else {
                return Write.notfound;
            }
        } catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean temporary() {
        return false;
    }
}
