package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SegmentingOutputStream extends ProxyOutputStream {
    private static final Logger log = LogManager.getLogger(SegmentingOutputStream.class);

    private final Long threshold;
    private Long written = 0L;

    /**
     * Flag set to true if any bytes have been written to the proxy stream
     */
    private final AtomicBoolean after = new AtomicBoolean();
    private final AtomicBoolean close = new AtomicBoolean();

    private final OutputStream buffer;
    private final OutputStream proxy;

    public SegmentingOutputStream(final OutputStream proxy, final Long threshold, final OutputStream buffer) {
        super(proxy);
        this.buffer = buffer;
        this.proxy = proxy;
        this.threshold = TransferStatus.UNKNOWN_LENGTH == threshold ? Long.MAX_VALUE : threshold;
    }

    @Override
    public void write(final int b) throws IOException {
        checkThreshold(1);
        buffer.write(b);
        written++;
        this.afterWrite(1);
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array to this
     * output stream.
     *
     * @param b The array of bytes to be written.
     * @throws IOException if an error occurs.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        buffer.write(b);
        written += b.length;
        this.checkThreshold(b.length);
        this.afterWrite(b.length);
    }


    /**
     * Writes <code>len</code> bytes from the specified byte array starting at
     * offset <code>off</code> to this output stream.
     *
     * @param b   The byte array from which the data will be written.
     * @param off The start offset in the byte array.
     * @param len The number of bytes to write.
     * @throws IOException if an error occurs.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        buffer.write(b, off, len);
        written += len;
        this.checkThreshold(len);
        this.afterWrite(len);
    }

    @Override
    protected void afterWrite(final int n) {
        after.set(true);
    }

    protected void checkThreshold(final int count) throws IOException {
        if(written >= threshold) {
            this.reset();
            this.flush();
        }
    }

    /**
     * Copy from temporary buffer to output
     */
    @Override
    public void flush() throws IOException {
        proxy.flush();
    }

    @Override
    public void close() throws IOException {
        if(close.get()) {
            log.warn("Skip double close of stream {}", this);
            return;
        }
        try {
            if(written > 0L || !after.get()) {
                this.reset();
                this.flush();
            }
            proxy.close();
        }
        finally {
            close.set(true);
        }
    }

    protected void reset() {
        // Wait for trigger of next threshold
        this.written = 0L;
    }
}
