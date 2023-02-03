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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.DataObjInp;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.pub.io.PackingIrodsOutputStream;

public class IRODSWriteFeature extends AppendWriteFeature<ObjStat> {

    private final IRODSSession session;

    public IRODSWriteFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<ObjStat> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            try {
                final IRODSFileSystemAO fs = session.getClient();
                final IRODSFileOutputStream out = fs.getIRODSFileFactory().instanceIRODSFileOutputStream(
                        file.getAbsolute(), status.isAppend() ? DataObjInp.OpenFlags.READ_WRITE : DataObjInp.OpenFlags.WRITE_TRUNCATE);
                return new StatusOutputStream<ObjStat>(new PackingIrodsOutputStream(out)) {
                    @Override
                    public ObjStat getStatus() throws BackgroundException {
                        // No remote attributes from server returned after upload
                        try {
                            return fs.getObjStat(file.getAbsolute());
                        }
                        catch(JargonException e) {
                            throw new IRODSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
                        }
                    }
                };
            }
            catch(JargonRuntimeException e) {
                if(e.getCause() instanceof JargonException) {
                    throw (JargonException) e.getCause();
                }
                throw new DefaultExceptionMappingService().map(e);
            }
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Uploading {0} failed", e, file);
        }
    }
}
