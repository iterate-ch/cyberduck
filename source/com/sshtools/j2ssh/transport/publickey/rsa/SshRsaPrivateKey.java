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
package com.sshtools.j2ssh.transport.publickey.rsa;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshRsaPrivateKey extends SshPrivateKey {
	RSAPrivateKey prvKey;
	RSAPublicKey pubKey;

	/**
	 * Creates a new SshRsaPrivateKey object.
	 *
	 * @param prv
	 * @param pub
	 */
	public SshRsaPrivateKey(RSAPrivateKey prv, RSAPublicKey pub) {
		prvKey = prv;
		pubKey = pub;
	}

	/**
	 * Creates a new SshRsaPrivateKey object.
	 *
	 * @param encoded
	 * @throws InvalidSshKeyException
	 */
	public SshRsaPrivateKey(byte[] encoded) throws InvalidSshKeyException {
		try {
			// Extract the key information
			ByteArrayReader bar = new ByteArrayReader(encoded);

			// Read the public key
			String header = bar.readString();

			if(!header.equals(getAlgorithmName())) {
				throw new InvalidSshKeyException();
			}

			BigInteger e = bar.readBigInteger();
			BigInteger n = bar.readBigInteger();

			// Read the private key
			BigInteger p = bar.readBigInteger();
			RSAPrivateKeySpec prvSpec = new RSAPrivateKeySpec(n, p);
			RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(n, e);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			prvKey = (RSAPrivateKey)kf.generatePrivate(prvSpec);
			pubKey = (RSAPublicKey)kf.generatePublic(pubSpec);
		}
		catch(Exception e) {
			throw new InvalidSshKeyException();
		}
	}

	/**
	 * @param obj
	 * @return
	 */
	public boolean equals(Object obj) {
		if(obj instanceof SshRsaPrivateKey) {
			return prvKey.equals(((SshRsaPrivateKey)obj).prvKey);
		}

		return false;
	}

	/**
	 * @return
	 */
	public int hashCode() {
		return prvKey.hashCode();
	}

	/**
	 * @return
	 */
	public String getAlgorithmName() {
		return "ssh-rsa";
	}

	/**
	 * @return
	 */
	public int getBitLength() {
		return prvKey.getModulus().bitLength();
	}

	/**
	 * @return
	 */
	public byte[] getEncoded() {
		try {
			ByteArrayWriter baw = new ByteArrayWriter();

			// The private key consists of the public key blob
			baw.write(getPublicKey().getEncoded());

			// And the private data
			baw.writeBigInteger(prvKey.getPrivateExponent());

			return baw.toByteArray();
		}
		catch(IOException ioe) {
			return null;
		}
	}

	/**
	 * @return
	 */
	public SshPublicKey getPublicKey() {
		return new SshRsaPublicKey(pubKey);
	}

	/**
	 * @param data
	 * @return
	 */
	public byte[] generateSignature(byte[] data) {
		try {
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initSign(prvKey);
			sig.update(data);

			ByteArrayWriter baw = new ByteArrayWriter();
			baw.writeString(getAlgorithmName());
			baw.writeBinaryString(sig.sign());

			return baw.toByteArray();
		}
		catch(Exception e) {
			return null;
		}
	}
}
