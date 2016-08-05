package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Symlink;

import java.io.IOException;

public class FTPSymlinkFeature implements Symlink {

    private FTPSession session;

    public FTPSymlinkFeature(FTPSession session) {
        this.session = session;
    }

    @Override
    public void symlink(Path file, String target) throws BackgroundException {
        try {
            if(!session.getClient().sendSiteCommand(String.format("SYMLINK %s %s", file.getAbsolute(), target))) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }
}
