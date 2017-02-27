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
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFile;

public class IRODSDirectoryFeature implements Directory<Void> {

    private final IRODSSession session;

    public IRODSDirectoryFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.getClient();
            final IRODSFile f = fs.getIRODSFileFactory().instanceIRODSFile(folder.getAbsolute());
            fs.mkdir(f, false);
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
        return folder;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }

    @Override
    public IRODSDirectoryFeature withWriter(final Write<Void> writer) {
        return this;
    }
}
