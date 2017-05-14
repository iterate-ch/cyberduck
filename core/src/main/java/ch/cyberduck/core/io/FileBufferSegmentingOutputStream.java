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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class FileBufferSegmentingOutputStream extends SegmentingOutputStream {
    private static final Logger log = Logger.getLogger(FileBufferSegmentingOutputStream.class);

    private final OutputStream proxy;
    private final Buffer buffer;

    public FileBufferSegmentingOutputStream(final OutputStream proxy, final Long threshold) {
        this(proxy, threshold, new FileBuffer());
    }

    public FileBufferSegmentingOutputStream(final OutputStream proxy, final Long threshold, final Buffer buffer) {
        super(proxy, threshold, new BufferOutputStream(buffer));
        this.proxy = proxy;
        this.buffer = buffer;
    }

    @Override
    public void close() throws IOException {
        super.close();
        buffer.close();
    }

    protected void copy() throws IOException {
        IOUtils.copy(new BufferInputStream(buffer), proxy);
        // Re-use buffer
        buffer.truncate(0L);
    }
}
