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

import ch.cyberduck.core.Path;

import java.io.IOException;

public interface Buffer {

    /**
     * @param chunk  Chunk to write to offset position
     * @param offset Target offset in buffer
     * @return Number of bytes written
     */
    int write(byte[] chunk, Long offset) throws IOException;

    /**
     * @param buffer Fill this buffer length
     * @param offset Position in buffer to read from
     * @return Length read. Should be equal input buffer length. -1 when there is no more data
     */
    int read(byte[] buffer, Long offset) throws IOException;

    /**
     * @return Current length of buffer
     */
    Long length();

    /**
     * Close and release resources for buffer. Subsequent operations will fail
     */
    void close();

    void truncate(Long length);

    interface Factory {
        Buffer create(Path file);
    }

    Buffer noop = new Buffer() {
        @Override
        public int write(final byte[] chunk, final Long offset) throws IOException {
            throw new IOException();
        }

        @Override
        public int read(final byte[] buffer, final Long offset) throws IOException {
            throw new IOException();
        }

        @Override
        public Long length() {
            return 0L;
        }

        @Override
        public void close() {
            // No-op
        }

        @Override
        public void truncate(final Long length) {
            // No-op
        }
    };
}
