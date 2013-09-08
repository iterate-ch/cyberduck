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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;

import java.io.IOException;

/**
 * @version $Id$
 */
public class SFTPDirectoryFeature implements Directory {

    private SFTPSession session;

    public SFTPDirectoryFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void mkdir(final Path file, final String region) throws BackgroundException {
        try {
            session.sftp().mkdir(file.getAbsolute(),
                    Integer.parseInt(new Permission(Preferences.instance().getInteger("queue.upload.permissions.folder.default")).getMode(), 8));
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}
