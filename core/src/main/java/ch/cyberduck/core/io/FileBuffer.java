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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;

public class FileBuffer implements Buffer {
    private static final Logger log = Logger.getLogger(FileBuffer.class);

    private final Local temporary;

    private RandomAccessFile file;

    public FileBuffer() {
        this.temporary = TemporaryFileServiceFactory.get().create(new AlphanumericRandomStringService().random());
    }

    @Override
    public synchronized int write(final byte[] chunk, final Long offset) throws IOException {
        final RandomAccessFile file = random();
        file.seek(offset);
        file.write(chunk, 0, chunk.length);
        return chunk.length;
    }

    @Override
    public synchronized int read(final byte[] chunk, final Long offset) throws IOException {
        final RandomAccessFile file = random();
        file.seek(offset);
        return file.read(chunk, 0, chunk.length);
    }

    @Override
    public synchronized Long length() {
        try {
            if(temporary.exists()) {
                final RandomAccessFile file = random();
                return file.length();
            }
            return 0L;
        }
        catch(IOException e) {
            log.error(String.format("Failure obtaining length for %s", this));
            return 0L;
        }
    }

    @Override
    public void truncate(final Long length) {
        if(temporary.exists()) {
            try {
                final RandomAccessFile file = random();
                file.setLength(length);
            }
            catch(IOException e) {
                log.warn(String.format("Failure truncating file %s to %d", temporary, length));
            }
        }
    }

    @Override
    public synchronized void close() {
        if(temporary.exists()) {
            try {
                final RandomAccessFile file = random();
                file.close();
            }
            catch(IOException e) {
                log.error(String.format("Failure closing buffer %s", this));
            }
            finally {
                try {
                    temporary.delete();
                }
                catch(AccessDeniedException e) {
                    log.warn(String.format("Failure removing temporary file %s for buffer %s. Schedule for delete on exit.", temporary, this));
                    Paths.get(temporary.getAbsolute()).toFile().deleteOnExit();
                }
            }
        }
    }

    protected synchronized RandomAccessFile random() throws IOException {
        if(null == file) {
            try {
                LocalTouchFactory.get().touch(temporary);
            }
            catch(AccessDeniedException e) {
                throw new IOException(e);
            }
            this.file = new RandomAccessFile(temporary.getAbsolute(), "rw");
            this.file.seek(0L);
        }
        return file;
    }
}
