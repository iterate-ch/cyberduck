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

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshKeyPair;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class SshRsaKeyPair extends SshKeyPair {
    private RSAPrivateKey prvKey;
    private RSAPublicKey pubKey;

    /**
     * Creates a new SshRsaKeyPair object.
     */
    public SshRsaKeyPair() {
    }

    /**
     *
     *
     * @param encoded
     *
     * @return
     *
     * @throws InvalidSshKeyException
     */
    public SshPrivateKey decodePrivateKey(byte[] encoded)
        throws InvalidSshKeyException {
        return new SshRsaPrivateKey(encoded);
    }

    /**
     *
     *
     * @param encoded
     *
     * @return
     *
     * @throws InvalidSshKeyException
     */
    public SshPublicKey decodePublicKey(byte[] encoded)
        throws InvalidSshKeyException {
        return new SshRsaPublicKey(encoded);
    }

    /**
     *
     *
     * @param bits
     */
    public void generate(int bits) {
        try {
            // Initialize the generator
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(bits, ConfigurationLoader.getRND());

            KeyPair pair = keyGen.generateKeyPair();

            // Get the keys and set
            setPrivateKey(new SshRsaPrivateKey(
                    (RSAPrivateKey) pair.getPrivate(),
                    (RSAPublicKey) pair.getPublic()));
        } catch (NoSuchAlgorithmException nsae) {
            prvKey = null;
            pubKey = null;
        }
    }
}
