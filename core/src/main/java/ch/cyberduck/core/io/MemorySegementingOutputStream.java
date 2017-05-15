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

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MemorySegementingOutputStream extends SegmentingOutputStream {
    private static final Logger log = Logger.getLogger(MemorySegementingOutputStream.class);

    private final OutputStream proxy;
    private final ByteArrayOutputStream buffer;

    public MemorySegementingOutputStream(final OutputStream proxy, final Integer threshold) {
        this(proxy, threshold, new ByteArrayOutputStream(threshold));
    }

    public MemorySegementingOutputStream(final OutputStream proxy, final Integer threshold, final ByteArrayOutputStream buffer) {
        super(proxy, (long) threshold, buffer);
        this.proxy = proxy;
        this.buffer = buffer;
    }

    @Override
    protected void copy() throws IOException {
        // Copy from memory file to output
        buffer.writeTo(proxy);
        // Re-use buffer
        buffer.reset();
    }
}