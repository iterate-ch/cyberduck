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

import com.sshtools.j2ssh.util.Hash;

import java.security.NoSuchAlgorithmException;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public abstract class SshPublicKey {
    /**
     *
     *
     * @return
     */
    public abstract String getAlgorithmName();

    /**
     *
     *
     * @return
     */
    public abstract int getBitLength();

    /**
     *
     *
     * @return
     */
    public abstract byte[] getEncoded();

    /**
     *
     *
     * @return
     */
    public String getFingerprint() {
        try {
            Hash md5 = new Hash("MD5");
            md5.putBytes(getEncoded());

            byte[] digest = md5.doFinal();
            int bits = getBitLength();
            bits = (((bits % 8) != 0) ? (bits += (bits % 8)) : bits);

            String ret = String.valueOf(bits);

            for (int i = 0; i < digest.length; i++) {
                ret += (((i == 0) ? ":" : "") + " " +
                Integer.toHexString(digest[i] & 0xFF));
            }

            return ret;
        } catch (NoSuchAlgorithmException nsae) {
            return null;
        }
    }

    /**
     *
     *
     * @param obj
     *
     * @return
     */
    public boolean equals(Object obj) {
        if (obj instanceof SshPublicKey) {
            return (getFingerprint().compareTo(((SshPublicKey) obj).getFingerprint()) == 0);
        }

        return false;
    }

    /**
     *
     *
     * @return
     */
    public int hashCode() {
        return getFingerprint().hashCode();
    }

    /**
     *
     *
     * @param signature
     * @param exchangeHash
     *
     * @return
     *
     * @throws InvalidSshKeySignatureException
     */
    public abstract boolean verifySignature(byte[] signature,
        byte[] exchangeHash) throws InvalidSshKeySignatureException;
}
