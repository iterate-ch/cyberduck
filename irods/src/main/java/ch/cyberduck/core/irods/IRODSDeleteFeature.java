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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;

import java.util.ArrayList;
import java.util.List;

public class IRODSDeleteFeature implements Delete {

    private final IRODSSession session;

    public IRODSDeleteFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> deleted = new ArrayList<Path>();
        for(Path file : files) {
            boolean skip = false;
            for(Path d : deleted) {
                if(file.isChild(d)) {
                    skip = true;
                    break;
                }
            }
            if(skip) {
                continue;
            }
            deleted.add(file);
            callback.delete(file);
            try {
                final IRODSFile f = session.getClient().getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
                if(!f.exists()) {
                    throw new NotfoundException(String.format("%s doesn't exist", file.getAbsolute()));
                }
                if(f.isFile()) {
                    session.getClient().fileDeleteForce(f);
                }
                else if(f.isDirectory()) {
                    session.getClient().directoryDeleteForce(f);
                }
            }
            catch(JargonException e) {
                throw new IRODSExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
