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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Symlink;

import java.io.IOException;

public class SFTPSymlinkFeature implements Symlink {

    private final SFTPSession session;

    public SFTPSymlinkFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void symlink(final Path file, String target) throws BackgroundException {
        try {
            session.sftp().symlink(target, file.getAbsolute());
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }
}
