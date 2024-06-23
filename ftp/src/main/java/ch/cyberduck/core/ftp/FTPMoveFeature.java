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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;

public class FTPMoveFeature implements Move {

    private final FTPSession session;
    private final Delete delete;

    public FTPMoveFeature(final FTPSession session) {
        this.session = session;
        this.delete = new FTPDeleteFeature(session);
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            if(status.isExists()) {
                delete.delete(Collections.singletonMap(renamed, status), connectionCallback, callback);
            }
            if(!session.getClient().rename(file.getAbsolute(), renamed.getAbsolute())) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
            // Copy original file attributes
            return renamed.withAttributes(file.attributes());
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path source, final Path directory, final String filename) {
        // Skip checking permission mask
    }
}
