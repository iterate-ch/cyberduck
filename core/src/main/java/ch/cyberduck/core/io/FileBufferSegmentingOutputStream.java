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

import org.apache.commons.io.output.NullOutputStream;
import org.apache.log4j.Logger;

import java.io.IOException;

public abstract class FileBufferSegmentingOutputStream extends SegmentingOutputStream {
    private static final Logger log = Logger.getLogger(FileBufferSegmentingOutputStream.class);

    private final Buffer buffer;

    public FileBufferSegmentingOutputStream(final Long threshold) {
        this(threshold, new FileBuffer());
    }

    public FileBufferSegmentingOutputStream(final Long threshold, final Buffer buffer) {
        super(new NullOutputStream(), threshold, new BufferOutputStream(buffer));
        this.buffer = buffer;
    }

    @Override
    public void close() throws IOException {
        super.close();
        buffer.close();
    }

    protected void copy() throws IOException {
        this.copy(buffer);
    }

    protected abstract void copy(final Buffer buffer) throws IOException;
}
