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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.PackingIrodsInputStream;

import java.io.InputStream;

public class IRODSReadFeature implements Read {

    private IRODSSession session;

    public IRODSReadFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final IRODSFileFactory factory = session.filesystem().getIRODSFileFactory();
            final IRODSFile f = factory.instanceIRODSFile(file.getAbsolute());
            if(f.exists()) {
                final InputStream in = new PackingIrodsInputStream(factory.instanceIRODSFileInputStream(f));
                if(status.isAppend()) {
                    return StreamCopier.skip(in, status.getOffset());
                }
                return in;
            }
            else {
                throw new NotfoundException(file.getAbsolute());
            }
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return true;
    }
}
