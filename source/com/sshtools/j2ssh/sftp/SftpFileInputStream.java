/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.io.*;

import java.io.*;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class SftpFileInputStream extends InputStream {
    private SftpFile file;
    private UnsignedInteger64 position = new UnsignedInteger64("0");

    /**
     * Creates a new SftpFileInputStream object.
     *
     * @param file
     *
     * @throws IOException
     */
    public SftpFileInputStream(SftpFile file) throws IOException {
        if (file.getHandle() == null) {
            throw new IOException("The file does not have a valid handle!");
        }

        if (file.getSFTPSubsystem() == null) {
            throw new IOException(
                "The file is not attached to an SFTP subsystem!");
        }

        this.file = file;
    }

    /**
     *
     *
     * @param buffer
     * @param offset
     * @param len
     *
     * @return
     *
     * @throws IOException
     */
    public int read(byte[] buffer, int offset, int len)
        throws IOException {
        int read = file.getSFTPSubsystem().readFile(file.getHandle(), position,
                buffer, offset, len);

        if (read > 0) {
            position = UnsignedInteger64.add(position, read);
        }

        return read;
    }

    /**
     *
     *
     * @return
     *
     * @throws java.io.IOException
     */
    public int read() throws java.io.IOException {
        byte[] buffer = new byte[1];
        int read = file.getSFTPSubsystem().readFile(file.getHandle(), position,
                buffer, 0, 1);
        position = UnsignedInteger64.add(position, read);

        return buffer[0] & 0xFF;
    }
	
	/*
	 * The skip method of InputStream creates a  byte array and then repeatedly reads into 
	 * it until n bytes have been read or the end of the stream has been reached. 
	 * Subclasses are  encouraged to provide a more efficient implementation of this method.
	 */
	public long skip(long n) {
		position = position.add(position, (int)n);
		return n;
	}

    /**
     *
     *
     * @throws IOException
     */
    public void close() throws IOException {
        file.close();
    }

    /**
     *
     *
     * @throws IOException
     */
    protected void finalize() throws IOException {
        if (file.getHandle() != null) {
            close();
        }
    }
}
