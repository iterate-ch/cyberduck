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
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFile;

public class IRODSDirectoryFeature implements Directory {

    private final IRODSSession session;

    public IRODSDirectoryFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null, new TransferStatus());
    }

    @Override
    public void mkdir(final Path file, final String region, final TransferStatus status) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.filesystem();
            final IRODSFile f = fs.getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
            fs.mkdir(f, false);
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}
