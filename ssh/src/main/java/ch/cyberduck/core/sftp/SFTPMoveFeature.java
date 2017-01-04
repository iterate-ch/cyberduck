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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import java.io.IOException;
import java.util.Collections;

public class SFTPMoveFeature implements Move {

    private final SFTPSession session;

    public SFTPMoveFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return true;
    }

    @Override
    public void move(final Path file, final Path renamed, boolean exists, final Delete.Callback callback) throws BackgroundException {
        try {
            if(exists) {
                final Delete delete = session.getFeature(Delete.class);
                delete.delete(Collections.singletonList(renamed), new DisabledLoginCallback(), callback);
            }
            session.sftp().rename(file.getAbsolute(), renamed.getAbsolute());
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }
}
