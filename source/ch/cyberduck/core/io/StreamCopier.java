package ch.cyberduck.core.io;

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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Id$
 */
public final class StreamCopier {
    private static final Logger log = Logger.getLogger(StreamCopier.class);

    private StreamCancelation cancel;

    private StreamProgress progress;

    private Integer chunksize;

    public StreamCopier(final StreamCancelation cancel, final StreamProgress progress) {
        this(cancel, progress, Preferences.instance().getInteger("connection.chunksize"));
    }

    public StreamCopier(final StreamCancelation cancel, final StreamProgress progress, final Integer chunksize) {
        this.cancel = cancel;
        this.progress = progress;
        this.chunksize = chunksize;
    }

    /**
     * Updates the current number of bytes transferred in the status reference.
     *
     * @param in       The stream to read from
     * @param offset   Skip bytes from input
     * @param out      The stream to write to
     * @param listener The stream listener to notify about bytes received and sent
     * @param limit    Transfer only up to this length
     */
    public void transfer(final InputStream in, final long offset, final OutputStream out,
                         final StreamListener listener, final long limit) throws IOException, ConnectionCanceledException {
        final BufferedInputStream bi = new BufferedInputStream(in);
        this.skip(offset, bi);
        final BufferedOutputStream bo = new BufferedOutputStream(out);
        try {
            final byte[] chunk = new byte[chunksize];
            long count = 0;
            while(!cancel.isCanceled()) {
                final int read = bi.read(chunk, 0, chunksize);
                if(-1 == read) {
                    if(log.isDebugEnabled()) {
                        log.debug("End of file reached");
                    }
                    break;
                }
                else {
                    listener.recv(read);
                    bo.write(chunk, 0, read);
                    progress.progress(read);
                    listener.sent(read);
                    count += read;
                    if(limit == count) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Limit %d reached reading from stream", limit));
                        }
                        break;
                    }
                }
            }
        }
        finally {
            bo.flush();
        }
        if(cancel.isCanceled()) {
            throw new ConnectionCanceledException();
        }
    }

    private void skip(final long offset, final BufferedInputStream bi) throws IOException {
        if(offset > 0) {
            long skipped = bi.skip(offset);
            if(log.isInfoEnabled()) {
                log.info(String.format("Skipping %d bytes", skipped));
            }
            if(skipped < offset) {
                throw new IOResumeException(String.format("Skipped %d bytes instead of %d",
                        skipped, offset));
            }
        }
    }
}
