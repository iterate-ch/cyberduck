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
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class BufferOutputStream extends ProxyOutputStream {
    private static final Logger log = LogManager.getLogger(BufferOutputStream.class.getName());

    private final Buffer buffer;

    private Long offset;

    public BufferOutputStream(final Buffer buffer) {
        this(NullOutputStream.NULL_OUTPUT_STREAM, buffer, 0L);
    }

    public BufferOutputStream(final Buffer buffer, final Long offset) {
        this(NullOutputStream.NULL_OUTPUT_STREAM, buffer, offset);
    }

    public BufferOutputStream(final OutputStream proxy, final Buffer buffer, final Long offset) {
        super(proxy);
        this.buffer = buffer;
        this.offset = offset;
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        this.write(buffer, 0, buffer.length);
    }

    @Override
    public void write(final byte[] bytes, final int off, final int len) throws IOException {
        log.debug("Buffer {} bytes at offset {}", len, offset);
        final byte[] chunk = new byte[len];
        System.arraycopy(bytes, off, chunk, 0, len);
        if(buffer.write(chunk, offset) != len) {
            log.warn("Failure buffering chunk of size {}", len);
        }
        super.write(bytes, off, len);
    }

    @Override
    protected void afterWrite(final int n) {
        offset += n;
    }
}
