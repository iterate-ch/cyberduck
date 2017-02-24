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
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class FTPReadFeature implements Read {
    private static final Logger log = Logger.getLogger(FTPReadFeature.class);

    private final FTPSession session;

    public FTPReadFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(!session.getClient().setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new FTPException(session.getClient().getReplyCode(), session.getClient().getReplyString());
            }
            if(status.isAppend()) {
                session.getClient().setRestartOffset(status.getOffset());
            }
            final InputStream in = new FTPDataFallback(session).data(new DataConnectionAction<InputStream>() {
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
            return new ReadReplyInputStream(in, status);
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        // Where a server process supports RESTart in STREAM mode
        try {
            return session.getClient().hasFeature("REST", "STREAM");
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    private final class ReadReplyInputStream extends ProxyInputStream {
        private final AtomicBoolean close;
        private final TransferStatus status;

        public ReadReplyInputStream(final InputStream proxy, final TransferStatus status) {
            super(proxy);
            this.status = status;
            this.close = new AtomicBoolean();
        }

        @Override
        public void close() throws IOException {
            if(close.get()) {
                log.warn(String.format("Skip double close of stream %s", this));
                return;
            }
            try {
                super.close();
                if(session.isConnected()) {
                    // Read 226 status after closing stream
                    int reply = session.getClient().getReply();
                    if(!FTPReply.isPositiveCompletion(reply)) {
                        final String text = session.getClient().getReplyString();
                        if(status.isSegment()) {
                            // Ignore 451 and 426 response because stream was prematurely closed
                            log.warn(String.format("Ignore unexpected reply %s when completing file segment %s", text, status));
                        }
                        else if(!status.isComplete()) {
                            log.warn(String.format("Ignore unexpected reply %s with incomplete transfer status %s", text, status));
                        }
                        else {
                            log.warn(String.format("Unexpected reply %s when completing file download with status %s", text, status));
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
