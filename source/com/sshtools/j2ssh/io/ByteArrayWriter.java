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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.math.BigInteger;


/**
 * Provides a reader type interface into the ByteArrayOutputStream
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 *
 * @created 20 December 2002
 */
public class ByteArrayWriter
    extends ByteArrayOutputStream {
    /**
     * Constructor for the ByteArrayWriter object
     */
    public ByteArrayWriter() {
    }

    /**
     * Writes a BigInteger to the array. The method first writes the length of
     * the encoded data and then the data itself.
     *
     * @param bi The BigInteger to write
     *
     * @exception IOException if the array cannot be written to
     */
    public void writeBigInteger(BigInteger bi)
                         throws IOException {
        byte raw[] = bi.toByteArray();

        writeInt(raw.length);

        write(raw);
    }

    /**
     * Writes a binary string to the byte array. The method first writes the
     * length of the data as an integer and then the data itself.
     *
     * @param data The data to write
     *
     * @exception IOException if the array cannot be written to
     */
    public void writeBinaryString(byte data[])
                           throws IOException {
        writeInt(data.length);

        write(data);
    }

    /**
     * <p>
     * Writes an integer into the byte array.
     * </p>
     *
     * <p>
     * NOTE: This will be replaced with the UnsignedInteger class
     * </p>
     *
     * @param i A long value to write (will be truncated if greater than max
     *        integer size)
     *
     * @exception IOException Thrown if the array cannot be written to
     */
    public void writeInt(long i)
                  throws IOException {
        byte raw[] = new byte[4];

        raw[0] = (byte) (i >> 24);
        raw[1] = (byte) (i >> 16);
        raw[2] = (byte) (i >> 8);
        raw[3] = (byte) (i);

        write(raw);
    }

    /**
     * <p>
     * Writes an integer into the byte array.
     * </p>
     *
     * <p>
     * NOTE: This will be replaced with the UnsignedInteger class
     * </p>
     *
     * @param i The integer value
     *
     * @exception IOException if the array cannot be written to
     */
    public void writeInt(int i)
                  throws IOException {
        byte raw[] = new byte[4];

        raw[0] = (byte) (i >> 24);
        raw[1] = (byte) (i >> 16);
        raw[2] = (byte) (i >> 8);
        raw[3] = (byte) (i);

        write(raw);
    }

    /**
     * Converts an integer into a byte array
     * @param value the integer calue to encode in the byte array
     * @return an encoded byte array
     */
    public static byte[] encodeInt(int i) {

        byte raw[] = new byte[4];

        raw[0] = (byte) (i >> 24);
        raw[1] = (byte) (i >> 16);
        raw[2] = (byte) (i >> 8);
        raw[3] = (byte) (i);

        return raw;
    }

    /**
     * Writes a 32bit unsigned integer to the byte array
     * @param value the unsigned integer value
     * @throws if the data cannot be written
     */
    public void writeUINT32(UnsignedInteger32 value) throws IOException {
      writeInt(value.longValue());
    }

    /**
     * Writes a 64bit unsigned integer to the byte array
     * @param value the unsigned integer valie
     * @throws IOException  if the data cannot be written
     */
    public void writeUINT64(UnsignedInteger64 value) throws IOException {
      byte raw[] = new byte[8];
      byte bi[] = value.bigIntValue().toByteArray();
      System.arraycopy(bi,0,raw,raw.length-bi.length,bi.length);
      // Pad the raw data
      write(raw);
    }

    /**
     * Writes an integer into an array, starting at the position specified
     *
     * @param array The array to write to
     * @param pos The starting position
     * @param value The integer value
     *
     * @throws IOException if there is not at least 4 bytes of data to write to
     *         from pos to the end of the array
     */
    public static void writeIntToArray(byte array[], int pos, int value)
                                throws IOException {
        if ((array.length - pos)<4) {
            throw new IOException("Not enough data in array to write integer at position "
                                  + String.valueOf(pos));
        }

        array[pos] = (byte) (value >> 24);
        array[pos + 1] = (byte) (value >> 16);
        array[pos + 2] = (byte) (value >> 8);
        array[pos + 3] = (byte) (value);
    }

    /**
     * Writes a string to the byte array. The method first writes the length of
     * the string as an integer and then the string data.
     *
     * @param str The string to write
     *
     * @exception IOException if the array cannot be written to
     */
    public void writeString(String str)
                     throws IOException {
        writeInt(str.length());

        write(str.getBytes());
    }

}
