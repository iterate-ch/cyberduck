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
package com.sshtools.j2ssh.transport.publickey.dsa;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshKeyPair;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


/**
 * Implements a j2ssh key pair for the ssh-dss public key algortithm.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SshDssKeyPair
    extends SshKeyPair {
    /**
     * Decodes a private key blob into an <code>SshPrivateKey</code> instance.
     *
     * @param encoded the encoded key
     *
     * @return a private key instance
     *
     * @throws InvalidSshKeyException if the key is invalid
     */
    public SshPrivateKey decodePrivateKey(byte encoded[])
                                   throws InvalidSshKeyException {
        return new SshDssPrivateKey(encoded);
    }

    /**
     * Decodes an SSH protocol specification public key blob into an
     * <code>SshPublicKey</code>.
     *
     * @param encoded the encoded key blob
     *
     * @return the public key instance
     *
     * @throws InvalidSshKeyException if the key is invalid
     */
    public SshPublicKey decodePublicKey(byte encoded[])
                                 throws InvalidSshKeyException {
        return new SshDssPublicKey(encoded);
    }

    /**
     * Initializes this instance with a newly generated public/private key pair
     *
     * @param bits the desired bit count of the key
     */
    public void generate(int bits) {
        try {
            // Initialize the generator
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
            keyGen.initialize(bits, new SecureRandom());

            KeyPair pair = keyGen.generateKeyPair();

            // Get the keys
            DSAPrivateKey prvKey = (DSAPrivateKey) pair.getPrivate();
            DSAPublicKey pubKey = (DSAPublicKey) pair.getPublic();

            // Set the private key (the public is automatically generated)
            setPrivateKey(new SshDssPrivateKey(prvKey));
        } catch (NoSuchAlgorithmException nsae) {
        }
    }
}
