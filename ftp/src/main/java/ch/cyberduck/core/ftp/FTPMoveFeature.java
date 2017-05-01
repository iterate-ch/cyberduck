package ch.cyberduck.core.ftp;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import java.io.IOException;
import java.util.Collections;

public class FTPMoveFeature implements Move {

    private final FTPSession session;

    private Delete delete;

    public FTPMoveFeature(final FTPSession session) {
        this.session = session;
        this.delete = new FTPDeleteFeature(session);
    }

    @Override
    public void move(final Path file, final Path renamed, boolean exists, final Delete.Callback callback) throws BackgroundException {
        try {
            if(exists) {
                delete.delete(Collections.singletonList(renamed), new DisabledLoginCallback(), callback);
            }
            if(!session.getClient().rename(file.getAbsolute(), renamed.getAbsolute())) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
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

}