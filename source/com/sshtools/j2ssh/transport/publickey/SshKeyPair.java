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
 * Abstract key pair for all Ssh Public Key implementations
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class SshKeyPair {
    private SshPrivateKey prv;
    private SshPublicKey pub;

    /**
     * Construct the key pair
     */
    public SshKeyPair() {
    }

    /**
     * Generates a key pair
     *
     * @param bits The required bit length
     */
    public abstract void generate(int bits);

    /**
     * Sets the Private Key for this pair (should also change the public key as
     * we requrie that the public key be saved within the private key)
     *
     * @param key the private key
     */
    public void setPrivateKey(SshPrivateKey key) {
        this.prv = key;
        this.pub = key.getPublicKey();
    }

    /**
     * Sets the private key
     *
     * @param encoded the encoded key
     *
     * @return the private key instance
     *
     * @throws InvalidSshKeyException if the encoded data is invalid
     */
    public SshPrivateKey setPrivateKey(byte encoded[])
                                throws InvalidSshKeyException {
        setPrivateKey(decodePrivateKey(encoded));

        return this.prv;
    }

    /**
     * Returns the Private Key
     *
     * @return the key pairs private key
     */
    public SshPrivateKey getPrivateKey() {
        return prv;
    }

    /**
     * Sets the Public key for this pair, this method removes the private key
     *
     * @param encoded the encoded public key
     *
     * @return the public key instance
     *
     * @throws InvalidSshKeyException if the encoded data is invalid
     */
    public SshPublicKey setPublicKey(byte encoded[])
                              throws InvalidSshKeyException {
        this.pub = decodePublicKey(encoded);
        this.prv = null;

        return this.pub;
    }

    /**
     * Returns the Public Key
     *
     * @return the public key instance
     */
    public SshPublicKey getPublicKey() {
        return pub;
    }

    /**
     * Decodes the private key
     *
     * @param encoded the encoded private key
     *
     * @return the private key instance
     *
     * @throws InvalidSshKeyException if the encoded data is invalid
     */
    public abstract SshPrivateKey decodePrivateKey(byte encoded[])
                                            throws InvalidSshKeyException;

    /**
     * Decodes the public key
     *
     * @param encoded the encoded public key
     *
     * @return the public key instance
     *
     * @throws InvalidSshKeyException if the encoded data is invalid
     */
    public abstract SshPublicKey decodePublicKey(byte encoded[])
                                          throws InvalidSshKeyException;
}
