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
package com.sshtools.j2ssh.transport.publickey.dsa;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshKeyPair;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshDssKeyPair extends SshKeyPair {
	/**
	 * @param encoded
	 * @return
	 * @throws InvalidSshKeyException
	 */
	public SshPrivateKey decodePrivateKey(byte[] encoded)
	    throws InvalidSshKeyException {
		return new SshDssPrivateKey(encoded);
	}

	/**
	 * @param encoded
	 * @return
	 * @throws InvalidSshKeyException
	 */
	public SshPublicKey decodePublicKey(byte[] encoded)
	    throws InvalidSshKeyException {
		return new SshDssPublicKey(encoded);
	}

	/**
	 * @param bits
	 */
	public void generate(int bits) {
		try {
			// Initialize the generator
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			keyGen.initialize(bits, ConfigurationLoader.getRND());

			KeyPair pair = keyGen.generateKeyPair();

			// Get the keys
			DSAPrivateKey prvKey = (DSAPrivateKey)pair.getPrivate();
			DSAPublicKey pubKey = (DSAPublicKey)pair.getPublic();

			// Set the private key (the public is automatically generated)
			setPrivateKey(new SshDssPrivateKey(prvKey));
		}
		catch(NoSuchAlgorithmException nsae) {
		}
	}
}
