/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.math.BigInteger;


/**
 * Provides a reader type interface into the ByteArrayInputStream.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 *
 * @created 20 December 2002
 */
public class ByteArrayReader
    extends ByteArrayInputStream {
    /**
     * Constructor for the ByteArrayReader object
     *
     * @param data The byte array to be read.
     */
    public ByteArrayReader(byte data[]) {
        super(data);
    }

    /**
     * Previews an integer in the byte array provided. The method reads 4 bytes
     * from the position in the array specified by start and returns the
     * integer value
     *
     * @param data The byte array to preview
     * @param start The start position in the array
     *
     * @return The int value
     */
    public static int readInt(byte data[], int start) {
        int ret =
            ((data[start] & 0xFF) << 24) | ((data[start + 1] & 0xFF) << 16)
            | ((data[start + 2] & 0xFF) << 8) | ((data[start + 3] & 0xFF) << 0);

        return ret;
    }

    /**
     * <p>
     * Reads an integer from the byte array.
     * </p>
     *
     * <p>
     * NOTE: This currently returns a long but only extracts 4 bytes. The
     * reason behind this is that this class has been designed for SSH byte
     * arrays; SSH uses unsigned integers but in java we do not have an
     * unsigned integer type. To safely return the non negative value we must
     * extract the byte information into a long to preserve the unsigned
     * value. THIS WILL BE UPDATED SOON WITH THE UnsignedInteger class.
     * </p>
     *
     * @return the value read
     *
     * @exception IOException Description of the Exception
     */
    public long readInt()
                 throws IOException {
        byte raw[] = new byte[4];
        read(raw);

        int ret =
            ((raw[0] & 0xFF) << 24) | ((raw[1] & 0xFF) << 16)
            | ((raw[2] & 0xFF) << 8) | ((raw[3] & 0xFF) << 0);

        return ret;
    }

    /**
     * Reads a 32bit Unsigned Integer from the byte array
     * @return  the UnsignedInteger43 value
     * @throws IOException if the read operation fails to read any data
     */
    public UnsignedInteger32 readUINT32() throws IOException {
      return new UnsignedInteger32(readInt());
    }

    /**
     * Reads a 64bit Unsigned Integer from the byte array
     * @return  the UnsignedInteger64 value
     * @throws IOException if the read operation fails to read any data
     */
    public UnsignedInteger64 readUINT64() throws IOException {
      byte raw[] = new byte[8];
      read(raw);
      return new UnsignedInteger64(raw);
    }

    /**
     * Reads an SSH type binary string from a byte array
     *
     * @param data the byte array to read
     * @param start the starting position of the string
     *
     * @return the string value read
     */
    public static String readString(byte data[], int start) {
        int len = (int) readInt(data, start);
        byte chars[] = new byte[(int) len];
        System.arraycopy(data, start + 4, chars, 0, len);

        return new String(chars);
    }

    /**
     * Returns a BigInteger from the byte array. The methos first reads the
     * length of the BigInteger data and then reads and creates a BigInteger
     * from the data read.
     *
     * @return The BigInteger value read
     *
     * @exception IOException if the data cannot be read
     */
    public BigInteger readBigInteger()
                              throws IOException {
        int len = (int) readInt();

        byte raw[] = new byte[len];

        read(raw);

        return new BigInteger(raw);
    }

    /**
     * Returns a binary string from the byte array. The method first reads an
     * integer to obtain the length of the data and then reads the data which
     * is returned in the byte array
     *
     * @return The binary data read
     *
     * @exception IOException if the data cannot be read
     */
    public byte[] readBinaryString()
                            throws IOException {
        long len = readInt();
        byte raw[] = new byte[(int) len];
        read(raw);

        return raw;
    }

    /**
     * Reads a string from the byte array, the format of the string will be 4
     * bytes representing an int value for the lengh of the string and then
     * len bytes of data.
     *
     * @return A the string read from the byte array
     *
     * @exception IOException if the data cannot be read
     */
    public String readString()
                      throws IOException {
        long len = readInt();
        byte raw[] = new byte[(int) len];
        read(raw);

        return new String(raw);
    }
}
