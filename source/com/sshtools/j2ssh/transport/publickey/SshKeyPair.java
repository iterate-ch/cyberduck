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
package com.sshtools.j2ssh.transport.publickey;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public abstract class SshKeyPair {
    private SshPrivateKey prv;
    private SshPublicKey pub;

    /**
     * Creates a new SshKeyPair object.
     */
    public SshKeyPair() {
    }

    /**
     *
     *
     * @param bits
     */
    public abstract void generate(int bits);

    /**
     *
     *
     * @param key
     */
    public void setPrivateKey(SshPrivateKey key) {
        this.prv = key;
        this.pub = key.getPublicKey();
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
    public SshPrivateKey setPrivateKey(byte[] encoded)
        throws InvalidSshKeyException {
        setPrivateKey(decodePrivateKey(encoded));

        return this.prv;
    }

    /**
     *
     *
     * @return
     */
    public SshPrivateKey getPrivateKey() {
        return prv;
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
    public SshPublicKey setPublicKey(byte[] encoded)
        throws InvalidSshKeyException {
        this.pub = decodePublicKey(encoded);
        this.prv = null;

        return this.pub;
    }

    /**
     *
     *
     * @return
     */
    public SshPublicKey getPublicKey() {
        return pub;
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
    public abstract SshPrivateKey decodePrivateKey(byte[] encoded)
        throws InvalidSshKeyException;

    /**
     *
     *
     * @param encoded
     *
     * @return
     *
     * @throws InvalidSshKeyException
     */
    public abstract SshPublicKey decodePublicKey(byte[] encoded)
        throws InvalidSshKeyException;
}
