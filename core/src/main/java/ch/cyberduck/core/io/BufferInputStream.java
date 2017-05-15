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

import java.io.IOException;
import java.io.InputStream;

public class BufferInputStream extends InputStream {

    private final Buffer buffer;

    private Long offset = 0L;

    public BufferInputStream(final Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(final byte[] bytes, final int off, final int len) throws IOException {
        final byte[] chunk = new byte[len];
        final int read = buffer.read(chunk, offset);
        if(read > 0) {
            offset += read;
            System.arraycopy(chunk, 0, bytes, off, read);
        }
        return read;
    }
}
