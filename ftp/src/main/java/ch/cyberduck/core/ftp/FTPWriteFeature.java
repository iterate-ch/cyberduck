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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class FTPWriteFeature implements Write<Void> {
    private static final Logger log = LogManager.getLogger(FTPWriteFeature.class);

    private final FTPSession session;

    public FTPWriteFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(!session.getClient().setFileType(FTPClient.BINARY_FILE_TYPE)) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
            final OutputStream out = new DataConnectionActionExecutor(session).data(new DataConnectionAction<OutputStream>() {
                @Override
                public OutputStream execute() throws BackgroundException {
                    try {
                        if(status.isAppend()) {
                            if(!status.isExists()) {
                                log.warn("Allocate {} bytes for file {}", status.getOffset(), file);
                                session.getClient().allocate((int) status.getOffset());
                            }
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
            });
            return new ReadReplyOutputStream(out, status);
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    public final class ReadReplyOutputStream extends VoidStatusOutputStream {
        private final AtomicBoolean close;
        private final TransferStatus status;

        public ReadReplyOutputStream(final OutputStream proxy, final TransferStatus status) {
            super(proxy);
            this.status = status;
            this.close = new AtomicBoolean();
        }

        @Override
        public void close() throws IOException {
            if(close.get()) {
                log.warn("Skip double close of stream {}", this);
                return;
            }
            try {
                super.close();
                if(session.isConnected()) {
                    // Read 226 status after closing stream
                    if(!FTPReply.isPositiveCompletion(session.getClient().getReply())) {
                        final String text = session.getClient().getReplyString();
                        if(status.isSegment()) {
                            // Ignore 451 and 426 response because stream was prematurely closed
                            log.warn("Ignore unexpected reply {} when completing file segment {}", text, status);
                        }
                        else if(!status.isComplete()) {
                            log.warn("Ignore unexpected reply {} with incomplete transfer status {}", text, status);
                        }
                        else {
                            log.warn("Unexpected reply {} when completing file download with status {}", text, status);
                            throw new FTPException(session.getClient().getReplyCode(), text);
                        }
                    }
                }
            }
            finally {
                close.set(true);
            }
        }
    }
}
