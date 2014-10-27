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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @version $Id$
 */
public class FTPWriteFeature extends AppendWriteFeature {
    private static final Logger log = Logger.getLogger(FTPWriteFeature.class);

    private FTPSession session;

    public FTPWriteFeature(final FTPSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(!session.getClient().setFileType(FTPClient.BINARY_FILE_TYPE)) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
            final OutputStream out = new FTPDataFallback(session).data(file, new DataConnectionAction<OutputStream>() {
                @Override
                public OutputStream execute() throws BackgroundException {
                    try {
                        if(status.isAppend()) {
                            return session.getClient().appendFileStream(file.getAbsolute());
                        }
                        else {
                            return session.getClient().storeFileStream(file.getAbsolute());
                        }
                    }
                    catch(IOException e) {
                        throw new FTPExceptionMappingService().map(e);
                    }
                }
            }, new DisabledProgressListener());
            return new CountingOutputStream(out) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    }
                    finally {
                        // Read 226 status
                        if(!session.getClient().completePendingCommand()) {
                            throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
                        }
                        if(this.getByteCount() != status.getLength()) {
                            // Interrupted transfer
                            if(!session.getClient().abort()) {
                                log.error("Error closing data socket:" + session.getClient().getReplyString());
                            }
                        }
                    }
                }
            };
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    @Override
    public boolean temporary() {
        return true;
    }
}
