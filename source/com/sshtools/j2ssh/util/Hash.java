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
package com.sshtools.j2ssh.util;

import com.sshtools.j2ssh.io.ByteArrayWriter;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * @author $author$
 * @version $Revision$
 */
public class Hash {
	private MessageDigest hash;

	/**
	 * Creates a new Hash object.
	 *
	 * @param algorithm
	 * @throws NoSuchAlgorithmException
	 */
	public Hash(String algorithm) throws NoSuchAlgorithmException {
		hash = MessageDigest.getInstance(algorithm);
	}

	/**
	 * @param bi
	 */
	public void putBigInteger(BigInteger bi) {
		byte[] data = bi.toByteArray();
		putInt(data.length);
		hash.update(data);
	}

	/**
	 * @param b
	 */
	public void putByte(byte b) {
		hash.update(b);
	}

	/**
	 * @param data
	 */
	public void putBytes(byte[] data) {
		hash.update(data);
	}

	/**
	 * @param i
	 */
	public void putInt(int i) {
		ByteArrayWriter baw = new ByteArrayWriter();

		try {
			baw.writeInt(i);
		}
		catch(IOException ioe) {
		}

		hash.update(baw.toByteArray());
	}

	/**
	 * @param str
	 */
	public void putString(String str) {
		putInt(str.length());
		hash.update(str.getBytes());
	}

	/**
	 *
	 */
	public void reset() {
		hash.reset();
	}

	/**
	 * @param data
	 * @param algorithm
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] simple(byte[] data, String algorithm)
	    throws NoSuchAlgorithmException {
		MessageDigest simp = MessageDigest.getInstance(algorithm);
		simp.update(data);

		return simp.digest();
	}

	/**
	 * @return
	 */
	public byte[] doFinal() {
		return hash.digest();
	}
}
