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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @version $Id$
 */
public class FTPWriteFeature implements Write {
    private static final Logger log = Logger.getLogger(FTPWriteFeature.class);

    private FTPSession session;

    public FTPWriteFeature(final FTPSession session) {
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
                public OutputStream execute() throws IOException {
                    if(status.isAppend()) {
                        return session.getClient().appendFileStream(file.getAbsolute());
                    }
                    else {
                        return session.getClient().storeFileStream(file.getAbsolute());
                    }
                }
            });
            return new CountingOutputStream(out) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    }
                    finally {
                        if(this.getByteCount() == status.getLength()) {
                            // Read 226 status
                            if(!session.getClient().completePendingCommand()) {
                                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
                            }
                        }
                        else {
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
            throw new FTPExceptionMappingService().map("Upload failed", e, file);
        }
    }

    @Override
    public Append append(final Path file, final Attributes feature) throws BackgroundException {
        if(new DefaultFindFeature(session).find(file)) {
            return new Append(feature.getAttributes(file).getSize());
        }
        return new Append();
    }
}
