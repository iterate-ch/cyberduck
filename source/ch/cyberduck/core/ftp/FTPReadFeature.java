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
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @version $Id$
 */
public class FTPReadFeature implements Read {
    private static final Logger log = Logger.getLogger(FTPReadFeature.class);

    private FTPSession session;

    public FTPReadFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(!session.getClient().setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
            if(status.isAppend()) {
                // Where a server process supports RESTart in STREAM mode
                if(!session.getClient().hasFeature("REST", "STREAM")) {
                    status.setAppend(false);
                }
                else {
                    session.getClient().setRestartOffset(status.getCurrent());
                }
            }
            final InputStream in = new FTPDataFallback(session).data(file, new DataConnectionAction<InputStream>() {
                @Override
                public InputStream execute() throws BackgroundException {
                    try {
                        return session.getClient().retrieveFileStream(file.getAbsolute());
                    }
                    catch(IOException e) {
                        throw new FTPExceptionMappingService().map(e);
                    }
                }
            }, new DisabledProgressListener());
            return new CountingInputStream(in) {
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
            throw new FTPExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean append(final Path file) {
        return true;
    }
}
