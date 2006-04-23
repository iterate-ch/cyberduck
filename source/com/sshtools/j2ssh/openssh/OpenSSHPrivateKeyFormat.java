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

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFormat;
import com.sshtools.j2ssh.util.SimpleASNReader;
import com.sshtools.j2ssh.util.SimpleASNWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;


/**
 * @author $author$
 * @version $Revision$
 */
public class OpenSSHPrivateKeyFormat implements SshPrivateKeyFormat {
	/**
	 * Creates a new OpenSSHPrivateKeyFormat object.
	 */
	public OpenSSHPrivateKeyFormat() {
	}

	/**
	 * @return
	 */
	public String getFormatType() {
		return "OpenSSH-PrivateKey";
	}

	/**
	 * @return
	 */
	public String toString() {
		return getFormatType();
	}

	/**
	 * @param formattedKey
	 * @param passphrase
	 * @return
	 * @throws InvalidSshKeyException
	 */
	public byte[] decryptKeyblob(byte[] formattedKey, String passphrase)
	    throws InvalidSshKeyException {
		//System.err.println("Decrypting key using passphrase " + passphrase);
		try {
			Reader r = new StringReader(new String(formattedKey, "US-ASCII"));
			PEMReader pem = new PEMReader(r);
			byte[] payload = pem.decryptPayload(passphrase);
			SimpleASNReader asn = new SimpleASNReader(payload);

			if(PEM.DSA_PRIVATE_KEY.equals(pem.getType())) {
				DSAKeyInfo keyInfo = DSAKeyInfo.getDSAKeyInfo(asn);
				ByteArrayWriter baw = new ByteArrayWriter();
				baw.writeString("ssh-dss");
				baw.writeBigInteger(keyInfo.getP());
				baw.writeBigInteger(keyInfo.getQ());
				baw.writeBigInteger(keyInfo.getG());
				baw.writeBigInteger(keyInfo.getX());

				return baw.toByteArray();
			}
			else if(PEM.RSA_PRIVATE_KEY.equals(pem.getType())) {
				RSAKeyInfo keyInfo = RSAKeyInfo.getRSAKeyInfo(asn);
				ByteArrayWriter baw = new ByteArrayWriter();
				baw.writeString("ssh-rsa");
				baw.writeBigInteger(keyInfo.getPublicExponent());
				baw.writeBigInteger(keyInfo.getModulus());
				baw.writeBigInteger(keyInfo.getPrivateExponent());

				return baw.toByteArray();
			}
			else {
				throw new InvalidSshKeyException("Unsupported type: "+
				    pem.getType());
			}
		}
		catch(GeneralSecurityException e) {
			//e.printStackTrace();
			throw new InvalidSshKeyException("Can't read key due to cryptography problems: "+e);
		}
		catch(IOException e) {
			//e.printStackTrace();
			throw new InvalidSshKeyException("Can't read key due to internal IO problems: "+e);
		}
	}

	/**
	 * @param keyblob
	 * @param passphrase
	 * @return
	 * @throws InvalidSshKeyException
	 */
	public byte[] encryptKeyblob(byte[] keyblob, String passphrase)
	    throws InvalidSshKeyException {
		try {
			ByteArrayReader bar = new ByteArrayReader(keyblob);
			String algorithm = bar.readString(); // dsa or rsa
			byte[] payload;
			PEMWriter pem = new PEMWriter();

			if("ssh-dss".equals(algorithm)) {
				BigInteger p = bar.readBigInteger();
				BigInteger q = bar.readBigInteger();
				BigInteger g = bar.readBigInteger();
				BigInteger x = bar.readBigInteger();
				DSAKeyInfo keyInfo = new DSAKeyInfo(p, q, g, x, BigInteger.ZERO);
				SimpleASNWriter asn = new SimpleASNWriter();
				DSAKeyInfo.writeDSAKeyInfo(asn, keyInfo);
				payload = asn.toByteArray();
				pem.setType(PEM.DSA_PRIVATE_KEY);
			}
			else if("ssh-rsa".equals(algorithm)) {
				BigInteger e = bar.readBigInteger();
				BigInteger n = bar.readBigInteger();
				BigInteger p = bar.readBigInteger();
				RSAKeyInfo keyInfo = new RSAKeyInfo(n, p, e, BigInteger.ZERO,
				    BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
				    BigInteger.ZERO);
				SimpleASNWriter asn = new SimpleASNWriter();
				RSAKeyInfo.writeRSAKeyInfo(asn, keyInfo);
				payload = asn.toByteArray();
				pem.setType(PEM.RSA_PRIVATE_KEY);
			}
			else {
				throw new InvalidSshKeyException("Unsupported J2SSH algorithm: "+algorithm);
			}

			pem.setPayload(payload);
			pem.encryptPayload(payload, passphrase);

			StringWriter w = new StringWriter();
			pem.write(w);

			return w.toString().getBytes("US-ASCII");
		}
		catch(GeneralSecurityException e) {
			//e.printStackTrace();
			throw new InvalidSshKeyException("Can't read key due to cryptography problems: "+e);
		}
		catch(IOException e) {
			//e.printStackTrace();
			throw new InvalidSshKeyException("Can't read key due to internal IO problems: "+e);
		}
	}

	/**
	 * @param formattedKey
	 * @return
	 */
	public boolean isFormatted(byte[] formattedKey) {
		try {
			Reader r = new StringReader(new String(formattedKey, "US-ASCII"));
			PEMReader pem = new PEMReader(r);

			return true;
		}
		catch(IOException e) {
			return false;
		}
	}

	/**
	 * @param formattedKey
	 * @return
	 */
	public boolean isPassphraseProtected(byte[] formattedKey) {
		try {
			Reader r = new StringReader(new String(formattedKey, "US-ASCII"));
			PEMReader pem = new PEMReader(r);

			return pem.getHeader().containsKey("DEK-Info");
		}
		catch(IOException e) {
			return true;
		}
	}

	/**
	 * @param algorithm
	 * @return
	 */
	public boolean supportsAlgorithm(String algorithm) {
		if("ssh-dss".equals(algorithm) || "ssh-rsa".equals(algorithm)) {
			return true;
		}
		else {
			return false;
		}
	}
}
