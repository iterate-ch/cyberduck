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
package com.sshtools.j2ssh.transport.publickey.rsa;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshKeyPair;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


/**
 * Implements a j2ssh key pair for the ssh-rsa public key algorithm
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SshRsaKeyPair
    extends SshKeyPair {
    private RSAPrivateKey prvKey;
    private RSAPublicKey pubKey;

    /**
     * Creates a new SshRsaKeyPair object.
     */
    public SshRsaKeyPair() {
    }

    /**
     * Decodes an encoded private key blob into a j2ssh private key instance
     *
     * @param encoded the encoded private key blob as defined in
     *        <code>SshRsaPrivateKey</code>
     *
     * @return the j2ssh private key instance
     *
     * @throws InvalidSshKeyException if the encoded key is invalid
     */
    public SshPrivateKey decodePrivateKey(byte encoded[])
                                   throws InvalidSshKeyException {
        return new SshRsaPrivateKey(encoded);
    }

    /**
     * Decodes an encoded public key blob into a j2ssh public key instance
     *
     * @param encoded the encoded public key blob as defined in
     *        <code>SshRsaPublicKey</code>
     *
     * @return the j2ssh public key instance
     *
     * @throws InvalidSshKeyException if the encoded key is invalid
     */
    public SshPublicKey decodePublicKey(byte encoded[])
                                 throws InvalidSshKeyException {
        return new SshRsaPublicKey(encoded);
    }

    /**
     * Initializes this instance with a newly generated public/private key
     * pair.
     *
     * @param bits the bit length required
     */
    public void generate(int bits) {
        try {
            // Initialize the generator
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(bits, new SecureRandom());

            KeyPair pair = keyGen.generateKeyPair();

            // Get the keys and set
            setPrivateKey(new SshRsaPrivateKey((RSAPrivateKey) pair.getPrivate(),
                                               (RSAPublicKey) pair.getPublic()));
        } catch (NoSuchAlgorithmException nsae) {
            prvKey = null;
            pubKey = null;
        }
    }
}
