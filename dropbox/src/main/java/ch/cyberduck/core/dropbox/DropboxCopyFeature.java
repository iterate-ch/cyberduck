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
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.transfer.TransferStatus;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;

public class DropboxCopyFeature implements Copy {

    private final DropboxSession session;

    public DropboxCopyFeature(DropboxSession session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status) throws BackgroundException {
        try {
            // If the source path is a folder all its contents will be copied.
            new DbxUserFilesRequests(session.getClient()).copy(source.getAbsolute(), target.getAbsolute());
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Cannot copy {0}", e, source);
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
}
