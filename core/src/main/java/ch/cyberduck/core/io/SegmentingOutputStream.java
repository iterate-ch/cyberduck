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

import org.apache.commons.io.output.ThresholdingOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SegmentingOutputStream extends ThresholdingOutputStream {

    private final ByteArrayOutputStream buffer;
    private final OutputStream proxy;

    public SegmentingOutputStream(final OutputStream proxy, final int threshold) {
        super(threshold);
        this.buffer = new ByteArrayOutputStream(threshold);
        this.proxy = proxy;
    }

    @Override
    protected OutputStream getStream() throws IOException {
        return buffer;
    }

    @Override
    protected void thresholdReached() throws IOException {
        this.copy();
    }

    @Override
    public void close() throws IOException {
        if(this.getByteCount() > 0L) {
            this.copy();
        }
        proxy.close();
    }

    private void copy() throws IOException {
        buffer.writeTo(proxy);
        // Re-use buffer
        buffer.reset();
        // Wait for trigger of next threshold
        this.resetByteCount();
    }
}