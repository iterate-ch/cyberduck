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
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.io.OutputStream;

import ch.ethz.ssh2.SCPClient;

/**
 * @version $Id$
 */
public class SCPWriteFeature implements Write {

    private SFTPSession session;

    public SCPWriteFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        final SCPClient client = new SCPClient(session.getClient());
        try {
            client.setCharset(session.getEncoding());
            return client.put(file.getName(), status.getLength(),
                    file.getParent().getAbsolute(), "0640");

        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Upload failed", e, file);
        }
    }

    @Override
    public boolean isResumable() {
        return false;
    }
}
