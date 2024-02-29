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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemorySegementingOutputStream extends SegmentingOutputStream {
    private static final Logger log = LogManager.getLogger(MemorySegementingOutputStream.class);

    private final OutputStream proxy;
    private final ByteArrayOutputStream buffer;
    private final Integer threshold;

    private final AtomicBoolean close = new AtomicBoolean();

    public MemorySegementingOutputStream(final OutputStream proxy, final Integer threshold) {
        this(proxy, threshold, new ByteArrayOutputStream(threshold));
    }

    public MemorySegementingOutputStream(final OutputStream proxy, final Integer threshold, final ByteArrayOutputStream buffer) {
        super(proxy, (long) threshold, buffer);
        this.proxy = proxy;
        this.buffer = buffer;
        this.threshold = threshold;
    }

    @Override
    protected void checkThreshold(final int count) throws IOException {
        if(buffer.size() >= threshold) {
            this.reset();
            this.flush(false);
        }
    }

    @Override
    public void flush() throws IOException {
        log.warn(String.format("Flush stream %s", proxy));
        this.flush(true);
    }

    /**
     * @param force Write last segment to proxy regardless if threshold is reached
     */
    protected void flush(final boolean force) throws IOException {
        // Copy from memory file to output
        final byte[] content = buffer.toByteArray();
        // Reuse buffer
        buffer.reset();
        for(int offset = 0; offset < content.length; offset += threshold) {
            int len = Math.min(threshold, content.length - offset);
            final byte[] bytes = Arrays.copyOfRange(content, offset, offset + len);
            if(!force && len < threshold) {
                // Write to start of buffer
                this.write(bytes);
            }
            else {
                // Write out
                proxy.write(bytes);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if(close.get()) {
            log.warn(String.format("Skip double close of stream %s", this));
            return;
        }
        try {
            if(buffer.size() > 0) {
                proxy.write(buffer.toByteArray());
            }
            // Reuse buffer
            buffer.reset();
            super.close();
        }
        finally {
            close.set(true);
        }
    }
}
