package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2009 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.ethz.ssh2.sftp.SFTPv3FileHandle;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author David Kocher, dkocher@cyberduck.ch
 * @version $Id$
 */
public class SFTPOutputStream extends OutputStream {

    private SFTPv3FileHandle handle;

    /**
     * Offset (in bytes) in the file to write
     */
    private long writeOffset = 0;

    public SFTPOutputStream(SFTPv3FileHandle handle) {
        this.handle = handle;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * The general contract for <code>write(b, off, len)</code> is that
     * some of the bytes in the array <code>b</code> are written to the
     * output stream in order; element <code>b[off]</code> is the first
     * byte written and <code>b[off+len-1]</code> is the last byte written
     * by this operation.
     *
     * @see ch.ethz.ssh2.sftp.SFTPv3Client#upload(SFTPv3FileHandle,long,byte[],int,int)
     */
    @Override
    public void write(byte[] buffer, int offset, int len)
            throws IOException {
        // We can just blindly write the whole buffer at once.
        // if <code>len</code> &gt; 32768, then the write operation will
        // be split into multiple writes in SFTPv3Client#write.
        handle.getClient().upload(handle, writeOffset, buffer, offset, len);

        writeOffset += len;
    }

    @Override
    public void write(int b)
            throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte) b;
        handle.getClient().upload(handle, writeOffset, buffer, 0, 1);

        writeOffset += 1;
    }

    public long skip(long n) {
        writeOffset += n;
        return n;
    }

    @Override
    public void close()
            throws IOException {
        handle.getClient().closeFile(handle);
    }
}