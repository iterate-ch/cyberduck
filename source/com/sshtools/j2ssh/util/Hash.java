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
package com.sshtools.j2ssh.util;

import java.io.IOException;

import java.math.BigInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sshtools.j2ssh.io.ByteArrayWriter;


/**
 * <p>
 * Template helper class for Hash alogorithms, wraps the MessageDigest class
 * from the java.security package
 * </p>
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 *
 * @created 31 August 2002
 */
public class Hash {
    private MessageDigest hash;

    /**
     * Constructor for the Hash object
     *
     * @param algorithm The algorithm to use
     *
     * @exception NoSuchAlgorithmException Thrown if the algorithm is not
     *            supported
     */
    public Hash(String algorithm)
         throws NoSuchAlgorithmException {
        hash = MessageDigest.getInstance(algorithm);
    }

    /**
     * Puts a BigInteger into the hash. Writes the length as an integer and
     * then the BigInteger data
     *
     * @param bi The BigInteger to hash
     */
    public void putBigInteger(BigInteger bi) {
        byte data[] = bi.toByteArray();

        putInt(data.length);
        hash.update(data);
    }

    /**
     * Puts a byte into the hash
     *
     * @param b The byte to hash
     */
    public void putByte(byte b) {
        hash.update(b);
    }

    /**
     * Puts an array of bytes into the hash
     *
     * @param data The byte array to hash
     */
    public void putBytes(byte data[]) {
        hash.update(data);
    }

    /**
     * Puts an integer into the hash.
     *
     * @param i The integer value
     */
    public void putInt(int i) {
        ByteArrayWriter baw = new ByteArrayWriter();

        try {
            baw.writeInt(i);
        } catch (IOException ioe) {
        }

        hash.update(baw.toByteArray());
    }

    /**
     * Puts a string into the hash. Writes the length as an integer and then
     * the string data
     *
     * @param str The string to hash
     */
    public void putString(String str) {
        putInt(str.length());

        hash.update(str.getBytes());
    }

    /**
     * Resets the hash;
     */
    public void reset() {
        hash.reset();
    }

    /**
     * Puts the data into a new instance of the algorithm message digest and
     * returns the output.
     *
     * @param data The data to hash
     * @param algorithm The algorithm to use
     *
     * @return The hash output
     *
     * @exception NoSuchAlgorithmException Thrown of the algorithm is not
     *            supported
     */
    public static byte[] simple(byte data[], String algorithm)
                         throws NoSuchAlgorithmException {
        MessageDigest simp = MessageDigest.getInstance(algorithm);

        simp.update(data);

        return simp.digest();
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public byte[] doFinal() {
        return hash.digest();
    }
}
