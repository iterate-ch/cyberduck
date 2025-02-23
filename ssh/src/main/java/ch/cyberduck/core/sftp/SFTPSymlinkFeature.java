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
    public void symlink(final Path link, final String target) throws BackgroundException {
        try {
            // Reversal of arguments to SSH_FXP_SYMLINK
            // When OpenSSH's sftp-server was implemented, the order of the arguments
            // to the SSH_FXP_SYMLINK method was inadvertently reversed. Unfortunately,
            // the reversal was not noticed until the server was widely deployed. Since
            // fixing this to follow the specification would cause incompatibility, the
            // current order was retained. For correct operation, clients should send
            // SSH_FXP_SYMLINK as follows:
            //
            // uint32        id
            // string        targetpath
            // string        linkpath
            session.sftp().symlink(target, link.getAbsolute());
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot create {0}", e, link);
        }
    }
}
