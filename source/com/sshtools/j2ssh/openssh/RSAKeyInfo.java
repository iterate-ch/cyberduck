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
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;


/**
 * @author $author$
 * @version $Revision$
 */
public class RSAKeyInfo implements KeyInfo {
	private BigInteger modulus;
	private BigInteger publicExponent;
	private BigInteger privateExponent;
	private BigInteger primeP;
	private BigInteger primeQ;
	private BigInteger primeExponentP;
	private BigInteger primeExponentQ;
	private BigInteger crtCoefficient;

	/**
	 * Creates a new RSAKeyInfo object.
	 *
	 * @param modulus
	 * @param publicExponent
	 * @param privateExponent
	 * @param primeP
	 * @param primeQ
	 * @param primeExponentP
	 * @param primeExponentQ
	 * @param crtCoefficient
	 */
	public RSAKeyInfo(BigInteger modulus, BigInteger publicExponent,
	                  BigInteger privateExponent, BigInteger primeP, BigInteger primeQ,
	                  BigInteger primeExponentP, BigInteger primeExponentQ,
	                  BigInteger crtCoefficient) {
		this.modulus = modulus;
		this.publicExponent = publicExponent;
		this.privateExponent = privateExponent;
		this.primeP = primeP;
		this.primeQ = primeQ;
		this.primeExponentP = primeExponentP;
		this.primeExponentQ = primeExponentQ;
		this.crtCoefficient = crtCoefficient;
	}

	/**
	 * @return
	 */
	public KeySpec getPrivateKeySpec() {
		return new RSAPrivateKeySpec(modulus, privateExponent);

		//        return new RSAPrivateCrtKeySpec(
		//            modulus,
		//            publicExponent,
		//            privateExponent,
		//            primeP,
		//            primeQ,
		//            primeExponentP,
		//            primeExponentQ,
		//            crtCoefficient);
	}

	/**
	 * @return
	 */
	public KeySpec getPublicKeySpec() {
		return new RSAPublicKeySpec(modulus, publicExponent);
	}

	/**
	 * @return
	 */
	public BigInteger getCrtCoefficient() {
		return crtCoefficient;
	}

	/**
	 * @return
	 */
	public BigInteger getModulus() {
		return modulus;
	}

	/**
	 * @return
	 */
	public BigInteger getPrimeExponentP() {
		return primeExponentP;
	}

	/**
	 * @return
	 */
	public BigInteger getPrimeExponentQ() {
		return primeExponentQ;
	}

	/**
	 * @return
	 */
	public BigInteger getPrimeP() {
		return primeP;
	}

	/**
	 * @return
	 */
	public BigInteger getPrimeQ() {
		return primeQ;
	}

	/**
	 * @return
	 */
	public BigInteger getPrivateExponent() {
		return privateExponent;
	}

	/**
	 * @return
	 */
	public BigInteger getPublicExponent() {
		return publicExponent;
	}

	/**
	 * @param asn
	 * @return
	 * @throws IOException
	 */
	public static RSAKeyInfo getRSAKeyInfo(SimpleASNReader asn)
	    throws IOException {
		asn.assertByte(0x30); // SEQUENCE

		int length = asn.getLength();
		asn.assertByte(0x02); // INTEGER (version)

		byte[] version = asn.getData();
		asn.assertByte(0x02); // INTEGER ()

		byte[] modulus = asn.getData();
		asn.assertByte(0x02); // INTEGER ()

		byte[] publicExponent = asn.getData();
		asn.assertByte(0x02); // INTEGER ()

		byte[] privateExponent = asn.getData();
		asn.assertByte(0x02); // INTEGER ()

		byte[] primeP = asn.getData();
		asn.assertByte(0x02); // INTEGER ()

		byte[] primeQ = asn.getData();
		asn.assertByte(0x02); // INTEGER ()

		byte[] primeExponentP = asn.getData();
		asn.assertByte(0x02); // INTEGER ()

		byte[] primeExponentQ = asn.getData();
		asn.assertByte(0x02); // INTEGER ()

		byte[] crtCoefficient = asn.getData();

		return new RSAKeyInfo(new BigInteger(modulus),
		    new BigInteger(publicExponent), new BigInteger(privateExponent),
		    new BigInteger(primeP), new BigInteger(primeQ),
		    new BigInteger(primeExponentP), new BigInteger(primeExponentQ),
		    new BigInteger(crtCoefficient));
	}

	/**
	 * @param asn
	 * @param keyInfo
	 */
	public static void writeRSAKeyInfo(SimpleASNWriter asn, RSAKeyInfo keyInfo) {
		// Write to a substream temporarily.
		// This code needs to know the length of the substream before it can write the data from
		// the substream to the main stream.
		SimpleASNWriter asn2 = new SimpleASNWriter();
		asn2.writeByte(0x02); // INTEGER (version)

		byte[] version = new byte[1];
		asn2.writeData(version);
		asn2.writeByte(0x02); // INTEGER ()
		asn2.writeData(keyInfo.getModulus().toByteArray());
		asn2.writeByte(0x02); // INTEGER ()
		asn2.writeData(keyInfo.getPublicExponent().toByteArray());
		asn2.writeByte(0x02); // INTEGER ()
		asn2.writeData(keyInfo.getPrivateExponent().toByteArray());
		asn2.writeByte(0x02); // INTEGER ()
		asn2.writeData(keyInfo.getPrimeP().toByteArray());
		asn2.writeByte(0x02); // INTEGER ()
		asn2.writeData(keyInfo.getPrimeQ().toByteArray());
		asn2.writeByte(0x02); // INTEGER ()
		asn2.writeData(keyInfo.getPrimeExponentP().toByteArray());
		asn2.writeByte(0x02); // INTEGER ()
		asn2.writeData(keyInfo.getPrimeExponentQ().toByteArray());
		asn2.writeByte(0x02); // INTEGER ()
		asn2.writeData(keyInfo.getCrtCoefficient().toByteArray());

		byte[] rsaKeyEncoded = asn2.toByteArray();
		asn.writeByte(0x30); // SEQUENCE
		asn.writeData(rsaKeyEncoded);
	}
}
