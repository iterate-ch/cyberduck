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
package com.sshtools.j2ssh.transport.publickey;

/**
 * Defines a Private Key
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class SshPrivateKey {
    /**
     * Constructs the private key
     */
    public SshPrivateKey() {
    }

    /**
     * Gets the alogrithm name for this private key
     *
     * @return the algorithm name
     */
    public abstract String getAlgorithmName();

    /**
     * Gets the bit length of this private key
     *
     * @return the bit length
     */
    public abstract int getBitLength();

    /**
     * Gets the SSH encoded key blob for this private key as defined by the
     * private key implementation
     *
     * @return the encoded key blob
     */
    public abstract byte[] getEncoded();

    /**
     * Gets the public key for this private key.<br>
     * <br>
     * This enables the API to determine the public key from its private data,
     * if the public key algorithm does not support generation of the public
     * key from private key ONLY data (i.e. it has a seperate public exponent
     * as in RSA) then the public data must be encoded within the private
     * encoded key blob to support this feature)
     *
     * @return the public key instance
     */
    public abstract SshPublicKey getPublicKey();

    /**
     * Generates a signature
     *
     * @param data The data for which the signature is generated
     *
     * @return the generated signature
     */
    public abstract byte[] generateSignature(byte data[]);
}
