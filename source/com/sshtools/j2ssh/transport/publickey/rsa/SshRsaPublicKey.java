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

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeySignatureException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshRsaPublicKey extends SshPublicKey {
	RSAPublicKey pubKey;

	/**
	 * Creates a new SshRsaPublicKey object.
	 *
	 * @param key
	 */
	public SshRsaPublicKey(RSAPublicKey key) {
		pubKey = key;
	}

	/**
	 * Creates a new SshRsaPublicKey object.
	 *
	 * @param encoded
	 * @throws InvalidSshKeyException
	 */
	public SshRsaPublicKey(byte[] encoded) throws InvalidSshKeyException {
		try {
			//this.hostKey = hostKey;
			RSAPublicKeySpec rsaKey;

			// Extract the key information
			ByteArrayReader bar = new ByteArrayReader(encoded);
			String header = bar.readString();

			if(!header.equals(getAlgorithmName())) {
				throw new InvalidSshKeyException();
			}

			BigInteger e = bar.readBigInteger();
			BigInteger n = bar.readBigInteger();
			rsaKey = new RSAPublicKeySpec(n, e);

			try {
				KeyFactory kf = KeyFactory.getInstance("RSA");
				pubKey = (RSAPublicKey)kf.generatePublic(rsaKey);
			}
			catch(NoSuchAlgorithmException nsae) {
				throw new InvalidSshKeyException();
			}
			catch(InvalidKeySpecException ikpe) {
				throw new InvalidSshKeyException();
			}
		}
		catch(IOException ioe) {
			throw new InvalidSshKeyException();
		}
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
		return pubKey.getModulus().bitLength();
	}

	/**
	 * @return
	 */
	public byte[] getEncoded() {
		try {
			ByteArrayWriter baw = new ByteArrayWriter();
			baw.writeString(getAlgorithmName());
			baw.writeBigInteger(pubKey.getPublicExponent());
			baw.writeBigInteger(pubKey.getModulus());

			return baw.toByteArray();
		}
		catch(IOException ioe) {
			return null;
		}
	}

	/**
	 * @param signature
	 * @param data
	 * @return
	 * @throws InvalidSshKeySignatureException
	 *
	 */
	public boolean verifySignature(byte[] signature, byte[] data)
	    throws InvalidSshKeySignatureException {
		try {
			// Check for older versions of the transport protocol
			if(signature.length != 128) {
				ByteArrayReader bar = new ByteArrayReader(signature);
				byte[] sig = bar.readBinaryString();
				String header = new String(sig);

				if(!header.equals(getAlgorithmName())) {
					throw new InvalidSshKeySignatureException();
				}

				signature = bar.readBinaryString();
			}

			Signature s = Signature.getInstance("SHA1withRSA");
			s.initVerify(pubKey);
			s.update(data);

			return s.verify(signature);
		}
		catch(NoSuchAlgorithmException nsae) {
			throw new InvalidSshKeySignatureException();
		}
		catch(IOException ioe) {
			throw new InvalidSshKeySignatureException();
		}
		catch(InvalidKeyException ike) {
			throw new InvalidSshKeySignatureException();
		}
		catch(SignatureException se) {
			throw new InvalidSshKeySignatureException();
		}
	}
}
