package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import com.dropbox.core.DbxException;

public class DropboxMoveFeature implements Move {


    private DropboxSession session;

    public DropboxMoveFeature(final DropboxSession dropboxSession) {
        this.session = dropboxSession;
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        try {
            this.session.getClient().files().move(file.getName(), renamed.getName());
        } catch (DbxException e) {
            throw new DropboxExceptionMappingService().map("Cannot move {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }
}
