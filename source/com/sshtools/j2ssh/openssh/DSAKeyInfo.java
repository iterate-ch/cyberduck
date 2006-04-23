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
package com.sshtools.j2ssh.openssh;

import com.sshtools.j2ssh.util.SimpleASNReader;
import com.sshtools.j2ssh.util.SimpleASNWriter;

import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.KeySpec;


/**
 * @author $author$
 * @version $Revision$
 */
public class DSAKeyInfo implements KeyInfo {
	private BigInteger p;
	private BigInteger q;
	private BigInteger g;
	private BigInteger x;
	private BigInteger y;

	/**
	 * Creates a new DSAKeyInfo object.
	 *
	 * @param p
	 * @param q
	 * @param g
	 * @param x
	 * @param y
	 */
	public DSAKeyInfo(BigInteger p, BigInteger q, BigInteger g, BigInteger x,
	                  BigInteger y) {
		this.p = p;
		this.q = q;
		this.g = g;
		this.x = x;
		this.y = y;
	}

	/**
	 * @return
	 */
	public BigInteger getG() {
		return g;
	}

	/**
	 * @return
	 */
	public BigInteger getP() {
		return p;
	}

	/**
	 * @return
	 */
	public BigInteger getQ() {
		return q;
	}

	/**
	 * @return
	 */
	public BigInteger getX() {
		return x;
	}

	/**
	 * @return
	 */
	public BigInteger getY() {
		return y;
	}

	/**
	 * @return
	 */
	public KeySpec getPrivateKeySpec() {
		return new DSAPrivateKeySpec(x, p, q, g);
	}

	/**
	 * @return
	 */
	public KeySpec getPublicKeySpec() {
		return new DSAPublicKeySpec(y, p, q, g);
	}

	/**
	 * @param asn
	 * @return
	 * @throws IOException
	 */
	public static DSAKeyInfo getDSAKeyInfo(SimpleASNReader asn)
	    throws IOException {
		asn.assertByte(0x30); // SEQUENCE

		int length = asn.getLength();
		asn.assertByte(0x02); // INTEGER (version)

		byte[] version = asn.getData();
		asn.assertByte(0x02); // INTEGER (p)

		byte[] paramP = asn.getData();
		asn.assertByte(0x02); // INTEGER (q)

		byte[] paramQ = asn.getData();
		asn.assertByte(0x02); // INTEGER (g)

		byte[] paramG = asn.getData();
		asn.assertByte(0x02); // INTEGER (y)

		byte[] paramY = asn.getData();
		asn.assertByte(0x02); // INTEGER (x)

		byte[] paramX = asn.getData();

		return new DSAKeyInfo(new BigInteger(paramP), new BigInteger(paramQ),
		    new BigInteger(paramG), new BigInteger(paramX),
		    new BigInteger(paramY));
	}

	/**
	 * @param asn
	 * @param keyInfo
	 */
	public static void writeDSAKeyInfo(SimpleASNWriter asn, DSAKeyInfo keyInfo) {
		// Write to a substream temporarily.
		// This code needs to know the length of the substream before it can write the data from
		// the substream to the main stream.
		SimpleASNWriter asn2 = new SimpleASNWriter();
		asn2.writeByte(0x02); // INTEGER (version)

		byte[] version = new byte[1];
		asn2.writeData(version);
		asn2.writeByte(0x02); // INTEGER (p)
		asn2.writeData(keyInfo.getP().toByteArray());
		asn2.writeByte(0x02); // INTEGER (q)
		asn2.writeData(keyInfo.getQ().toByteArray());
		asn2.writeByte(0x02); // INTEGER (g)
		asn2.writeData(keyInfo.getG().toByteArray());
		asn2.writeByte(0x02); // INTEGER (y)
		asn2.writeData(keyInfo.getY().toByteArray());
		asn2.writeByte(0x02); // INTEGER (x)
		asn2.writeData(keyInfo.getX().toByteArray());

		byte[] dsaKeyEncoded = asn2.toByteArray();
		asn.writeByte(0x30); // SEQUENCE
		asn.writeData(dsaKeyEncoded);
	}
}
