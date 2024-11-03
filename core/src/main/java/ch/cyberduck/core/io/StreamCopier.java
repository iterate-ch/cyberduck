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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class StreamCopier {
    private static final Logger log = LogManager.getLogger(StreamCopier.class);

    private final StreamCancelation cancel;
    private final StreamProgress progress;

    private StreamListener listener = new DisabledStreamListener();

    /**
     * Buffer size
     */
    private Integer chunksize
            = PreferencesFactory.get().getInteger("connection.chunksize");

    private Long offset = 0L;
    private Long limit = -1L;

    /**
     * Close input stream after copying
     */
    private boolean autoclose = true;

    public StreamCopier(final StreamCancelation cancel, final StreamProgress progress) {
        this.cancel = cancel;
        this.progress = progress;
    }

    public StreamCopier withChunksize(final Integer chunksize) {
        this.chunksize = chunksize;
        return this;
    }

    public StreamCopier withListener(final StreamListener listener) {
        this.listener = listener;
        return this;
    }

    public StreamCopier withLimit(final Long limit) {
        if(limit > 0) {
            this.limit = limit;
        }
        return this;
    }

    public StreamCopier withOffset(final Long offset) {
        if(offset > 0) {
            this.offset = offset;
        }
        return this;
    }

    public StreamCopier withAutoclose(final boolean autoclose) {
        this.autoclose = autoclose;
        return this;
    }

    /**
     * Updates the current number of bytes transferred in the status reference.
     *
     * @param in  The stream to read from
     * @param out The stream to write to
     */
    public void transfer(final InputStream in, final OutputStream out) throws BackgroundException {
        try {
            if(offset > 0) {
                skip(in, offset);
            }
            final byte[] buffer = new byte[chunksize];
            long total = 0;
            int len = chunksize;
            if(limit > 0 && limit < chunksize) {
                // Cast will work because chunk size is int
                len = limit.intValue();
            }
            while(len > 0) {
                cancel.validate();
                final int read = in.read(buffer, 0, len);
                if(-1 == read) {
                    log.debug("End of file reached with {} bytes read from stream", total);
                    progress.setComplete();
                    break;
                }
                else {
                    listener.recv(read);
                    out.write(buffer, 0, read);
                    listener.sent(read);
                    total += read;
                }
                if(limit > 0) {
                    // Only adjust if not reading to the end of the stream. Cast will work because chunk size is int
                    len = (int) Math.min(limit - total, chunksize);
                }
                if(limit == total) {
                    log.debug("Limit {} reached reading from stream", limit);
                    progress.setComplete();
                }
            }
            final StreamCloser c = new DefaultStreamCloser();
            c.close(out);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            if(autoclose) {
                final StreamCloser c = new DefaultStreamCloser();
                c.close(in);
            }
        }
    }

    public static InputStream skip(final InputStream in, final long offset) throws BackgroundException {
        try {
            if(offset > 0) {
                long skipped = in.skip(offset);
                log.info("Skipping {} bytes", skipped);
                if(skipped < offset) {
                    throw new IOResumeException(String.format("Skipped %d bytes instead of %d",
                            skipped, offset));
                }
            }
            return in;
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
