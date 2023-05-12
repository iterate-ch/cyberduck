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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;

public class FileBuffer implements Buffer {
    private static final Logger log = LogManager.getLogger(FileBuffer.class);

    private final Local temporary;

    private RandomAccessFile file;
    private Long length = 0L;

    public FileBuffer() {
        this(TemporaryFileServiceFactory.get().create(new AlphanumericRandomStringService().random()));
    }

    public FileBuffer(final Local temporary) {
        this.temporary = temporary;
    }

    @Override
    public synchronized int write(final byte[] chunk, final Long offset) throws IOException {
        final RandomAccessFile file = random();
        file.seek(offset);
        file.write(chunk, 0, chunk.length);
        this.length = Math.max(this.length, file.length());
        return chunk.length;
    }

    @Override
    public synchronized int read(final byte[] chunk, final Long offset) throws IOException {
        final RandomAccessFile file = random();
        if(offset < file.length()) {
            file.seek(offset);
            if(chunk.length + offset > file.length()) {
                final int bufferRead = file.read(chunk, 0, (int) (file.length() - offset));
                if(bufferRead + offset < file.length()) {
                    // Less read than we have in file - honour read interface
                    return bufferRead;
                }
                // Add null bytes up to allocated size
                final int missing = chunk.length - bufferRead;
                final int available = (int) Math.min(missing, this.length - file.length());
                final int nullRead = new NullInputStream(available).read(chunk, bufferRead, available);
                if(nullRead > 0) {
                    return bufferRead + nullRead;
                }
                return bufferRead;
            }
            else {
                return file.read(chunk, 0, chunk.length);
            }
        }
        else {
            final NullInputStream nullStream = new NullInputStream(this.length);
            if(nullStream.available() > 0) {
                nullStream.skip(offset);
                return nullStream.read(chunk, 0, chunk.length);
            }
            else {
                return IOUtils.EOF;
            }
        }
    }

    @Override
    public synchronized Long length() {
        return length;
    }

    @Override
    public void truncate(final Long length) {
        this.length = length;
        if(temporary.exists()) {
            try {
                final RandomAccessFile file = random();
                if(length < file.length()) {
                    // Truncate current
                    file.setLength(length);
                }
            }
            catch(IOException e) {
                log.warn(String.format("Failure truncating file %s to %d", temporary, length));
            }
        }
    }

    @Override
    public synchronized void close() {
        this.length = 0L;
        if(temporary.exists()) {
            try {
                if(file != null) {
                    file.close();
                }
            }
            catch(IOException e) {
                log.error(String.format("Failure closing buffer %s", this));
            }
            finally {
                try {
                    temporary.delete();
                    file = null;
                }
                catch(AccessDeniedException | NotfoundException e) {
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileBuffer{");
        sb.append("temporary=").append(temporary);
        sb.append(", length=").append(length);
        sb.append('}');
        return sb.toString();
    }
}
