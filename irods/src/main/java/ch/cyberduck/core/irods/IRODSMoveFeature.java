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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFile;

import java.util.Collections;

public class IRODSMoveFeature implements Move {

    private final IRODSSession session;
    private Delete delete;

    public IRODSMoveFeature(IRODSSession session) {
        this.session = session;
        this.delete = new IRODSDeleteFeature(session);
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.getClient();
            final IRODSFile s = fs.getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
            if(!s.exists()) {
                throw new NotfoundException(String.format("%s doesn't exist", file.getAbsolute()));
            }
            if(exists) {
                delete.delete(Collections.singletonList(renamed), new DisabledLoginCallback(), callback);
            }
            final IRODSFile d = fs.getIRODSFileFactory().instanceIRODSFile(renamed.getAbsolute());
            s.renameTo(d);
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return true;
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

    @Override
    public Move withList(final ListService list) {
        return this;
    }
}
