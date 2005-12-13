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

import java.io.IOException;
import java.io.OutputStream;

import com.sshtools.j2ssh.io.UnsignedInteger64;


/**
 * @author $author$
 * @version $Revision$
 */
public class SftpFileOutputStream extends OutputStream {
	private SftpFile file;
	private UnsignedInteger64 position = new UnsignedInteger64("0");

	/**
	 * Creates a new SftpFileOutputStream object.
	 *
	 * @param file
	 * @throws IOException
	 */
	public SftpFileOutputStream(SftpFile file) throws IOException {
		if(file.getHandle() == null) {
			throw new IOException("The file does not have a valid handle!");
		}

		if(file.getSFTPSubsystem() == null) {
			throw new IOException("The file is not attached to an SFTP subsystem!");
		}

		this.file = file;
	}

	/**
	 * @param buffer
	 * @param offset
	 * @param len
	 * @throws IOException
	 */
	public void write(byte[] buffer, int offset, int len)
	    throws IOException {
		int pos = 0;
		int count;
		int available;
		int max = (int)file.getSFTPSubsystem().maximumPacketSize();

		while(pos < len) {
			available = ((int)file.getSFTPSubsystem().availableWindowSpace() < max)
			            ? (int)file.getSFTPSubsystem().availableWindowSpace() : max;
			count = (available < (len-pos)) ? available : (len-pos);
			file.getSFTPSubsystem().writeFile(file.getHandle(), position,
			    buffer, offset+pos, count);
			position = UnsignedInteger64.add(position, count);
			pos += count;
		}
	}

	/**
	 * @param b
	 * @throws IOException
	 */
	public void write(int b) throws IOException {
		byte[] buffer = new byte[1];
		buffer[0] = (byte)b;
		file.getSFTPSubsystem().writeFile(file.getHandle(), position, buffer,
		    0, 1);
		position = UnsignedInteger64.add(position, 1);
	}

	public long skip(long n) {
		position = UnsignedInteger64.add(position, (int)n);
		return n;
	}

	/**
	 * @throws IOException
	 */
	public void close() throws IOException {
		file.close();
	}

	/**
	 * @throws IOException
     */
    protected void finalize() throws Throwable {
        try {
            if(file.getHandle() != null) {
                close();
            }
        }
        finally {
            super.finalize();
        }
    }
}
