/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport.publickey;

import java.security.NoSuchAlgorithmException;
import com.sshtools.j2ssh.util.Hash;

/**
 *  Defines a public key mechanism for use within the SSH API
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public abstract class SshPublicKey {
    /**
     *  Gets the alogrithm name for this public key
     *
     *@return    the algorithm name
     */
    public abstract String getAlgorithmName();


    /**
     *  Gets the bit length of this public key
     *
     *@return    the bit length
     */
    public abstract int getBitLength();


    /**
     *  Gets the SSH encoded key blob for this public key as defined in
     *  [SSH-TRANS]
     *
     *@return    the encoded key blob
     */
    public abstract byte[] getEncoded();


    /**
     *  Gets the keys fingerprint as defined in draft-ietf-secsh-fingerprint.txt
     *
     *@return    The fingerprint
     */
    public String getFingerprint() {
        try {
            Hash md5 = new Hash("MD5");

            md5.putBytes(getEncoded());

            byte digest[] = md5.doFinal();

            int bits = getBitLength();

            bits = ((bits % 8 )!= 0 ? bits+= (bits % 8) : bits);
            String ret = String.valueOf(bits);

            for (int i = 0; i < digest.length; i++) {
                ret += (((i == 0) ? ":" : "") + " "
                        + Integer.toHexString(digest[i] & 0xFF));
            }

            return ret;
        } catch (NoSuchAlgorithmException nsae) {
            return null;
        }
    }


    /**
     *  Implement this method to verify the signature
     *
     *@param  signature                             The signature to verify
     *@param  exchangeHash                          The exchange hash output of
     *      key exchange
     *@return                                       The success of the
     *      verification
     *@exception  InvalidSshKeySignatureException   Description of the Exception
     */
    public abstract boolean verifySignature(byte signature[],
            byte exchangeHash[])
             throws InvalidSshKeySignatureException;
}
