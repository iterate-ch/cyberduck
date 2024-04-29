package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.concurrency.Interruptibles;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

public abstract class DelayedHttpEntity extends AbstractHttpEntity {
    private static final Logger log = LogManager.getLogger(DelayedHttpEntity.class);

    /**
     * Count down when stream to server has been opened
     */
    private final CountDownLatch streamOpen;

    /**
     * Count down when stream is closed writing entity to server
     */
    private final CountDownLatch streamClosed = new CountDownLatch(1);

    public DelayedHttpEntity() {
        this(Thread.currentThread(), new CountDownLatch(1));
    }

    /**
     * @param streamOpen Signal when stream is ready
     */
    public DelayedHttpEntity(final CountDownLatch streamOpen) {
        this(Thread.currentThread(), streamOpen);
    }

    public DelayedHttpEntity(final Thread parentThread, final CountDownLatch streamOpen) {
        this.parentThread = parentThread;
        this.streamOpen = streamOpen;
    }

    /**
     * HTTP stream to write to
     */
    private OutputStream stream;

    /**
     * Parent thread to check if still alive
     */
    private final Thread parentThread;

    public boolean isRepeatable() {
        return false;
    }

    public abstract long getContentLength();

    public InputStream getContent() throws IOException {
        throw new IOException(new UnsupportedOperationException("No content here"));
    }

    /**
     * @return The stream to write to after the entry signal was received.
     */
    public OutputStream getStream() {
        if(null == stream) {
            // Nothing to write
            return NullOutputStream.NULL_OUTPUT_STREAM;
        }
        return stream;
    }

    public void writeTo(final OutputStream out) throws IOException {
        try {
            // Signal when finished writing to stream
            stream = new ProxyOutputStream(out) {
                @Override
                public void close() throws IOException {
                    super.close();
                    streamClosed.countDown();
                }

                @Override
                protected void handleIOException(final IOException e) throws IOException {
                    streamClosed.countDown();
                    throw e;
                }
            };
        }
        finally {
            // Signal stream is ready for writing
            streamOpen.countDown();
        }
        // Wait for signal when content has been written to the pipe
        Interruptibles.await(streamClosed, IOException.class, new Interruptibles.ThreadAliveCancelCallback(parentThread));
    }

    public boolean isStreaming() {
        return true;
    }

    /**
     * @return Set when output stream is ready
     */
    public CountDownLatch getStreamOpen() {
        return streamOpen;
    }
}
