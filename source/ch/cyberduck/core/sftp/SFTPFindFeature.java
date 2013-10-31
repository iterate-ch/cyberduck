package ch.cyberduck.core.sftp;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import java.io.IOException;

import ch.ethz.ssh2.SFTPException;

/**
 * @version $Id$
 */
public class SFTPFindFeature implements Find {

    private SFTPSession session;

    public SFTPFindFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        try {
            try {
                // The SSH_FXP_REALPATH request can be used to have the server
                // canonicalize any given path name to an absolute path.
                return session.sftp().canonicalPath(file.getAbsolute()) != null;
            }
            catch(SFTPException e) {
                throw new SFTPExceptionMappingService().map(e);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        catch(NotfoundException e) {
            // We expect SSH_FXP_STATUS if the file is not found
            return false;
        }
    }

    @Override
    public Find withCache(Cache cache) {
        return this;
    }
}
