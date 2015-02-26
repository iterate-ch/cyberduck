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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Move;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;

/**
 * @version $Id$
 */
public class IRODSMoveFeature implements Move {

    private IRODSSession session;

    public IRODSMoveFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final ProgressListener listener) throws BackgroundException {
        try {
            if (!file.getAbsolute().equals(renamed.getAbsolute())) {
                final IRODSFile irodsSourceFile = session.getIrodsFileSystemAO().getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
                if (irodsSourceFile.exists()) {
                    final IRODSFile irodsDestinationFile = session.getIrodsFileSystemAO().getIRODSFileFactory().instanceIRODSFile(renamed.getAbsolute());
                    irodsSourceFile.renameTo(irodsDestinationFile);
                } else {
                    throw new NotfoundException(String.format("%s doesn't exist", file.getAbsolute()));
                }
            } else {
                // TODO research support for moving to different IRODSFile.resource
                throw new InteroperabilityException("Renaming to same name initiates a physical move to a different resource");
            }
        } catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }
}
