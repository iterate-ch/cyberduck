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
package com.sshtools.j2ssh.transport.publickey.rsa;

import java.io.IOException;

import java.math.BigInteger;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeySignatureException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  This class represents an RSA public key
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public class SshRsaPublicKey
         extends SshPublicKey {
    RSAPublicKey pubKey;


    /**
     *  Creates a new SshRsaPublicKey object.
     *
     *@param  key  a JCE RSA public key
     */
    public SshRsaPublicKey(RSAPublicKey key) {
        pubKey = key;
    }


    /**
     *  Creates a new SshRsaPublicKey object.
     *
     *@param  encoded                     the encoded public key
     *@exception  InvalidSshKeyException  Description of the Exception
     *@throws  InvalidSshKeyException     if the encoded key is invalid
     */
    public SshRsaPublicKey(byte encoded[])
             throws InvalidSshKeyException {
        try {
            //this.hostKey = hostKey;
            RSAPublicKeySpec rsaKey;

            // Extract the key information
            ByteArrayReader bar = new ByteArrayReader(encoded);

            String header = bar.readString();

            if (!header.equals(getAlgorithmName())) {
                throw new InvalidSshKeyException();
            }

            BigInteger e = bar.readBigInteger();
            BigInteger n = bar.readBigInteger();

            rsaKey = new RSAPublicKeySpec(n, e);

            try {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                pubKey = (RSAPublicKey) kf.generatePublic(rsaKey);
            } catch (NoSuchAlgorithmException nsae) {
                throw new InvalidSshKeyException();
            } catch (InvalidKeySpecException ikpe) {
                throw new InvalidSshKeyException();
            }
        } catch (IOException ioe) {
            throw new InvalidSshKeyException();
        }
    }


    /**
     *  Gets the algorithm name for this public key
     *
     *@return    "ssh-rsa"
     */
    public String getAlgorithmName() {
        return "ssh-rsa";
    }


    /**
     *  Gets the bit length for this key
     *
     *@return    the bit length
     */
    public int getBitLength() {
        return pubKey.getModulus().bitLength();
    }


    /**
     *  Gets the encodeed public key in the following format:<br>
     *  <br>
     *  String "ssh-rsa"<br>
     *  MPINT e<br>
     *  MPINT n<br>
     *
     *
     *@return    the encoded byte array
     */
    public byte[] getEncoded() {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();

            baw.writeString(getAlgorithmName());
            baw.writeBigInteger(pubKey.getPublicExponent());
            baw.writeBigInteger(pubKey.getModulus());

            return baw.toByteArray();
        } catch (IOException ioe) {
            return null;
        }
    }


    /**
     *  Verifies the signature over the data supplied, the signature format
     *  should be as follows:<br>
     *  <br>
     *  String "ssh-rsa"<br>
     *  String signature blob<br>
     *
     *
     *@param  signature                         the ssh encoded signature to
     *      verify in the above format
     *@param  data                              the data from which the
     *      signature was generated
     *@return                                   <tt>true</tt> if the signature
     *      is valid otherwise <tt>false</tt>
     *@throws  InvalidSshKeySignatureException  if the signature is in an
     *      invalid format
     */
    public boolean verifySignature(byte signature[], byte data[])
             throws InvalidSshKeySignatureException {
        try {
            ByteArrayReader bar = new ByteArrayReader(signature);

            byte sig[] = bar.readBinaryString();

            if (sig.length != 128) {
                // OpenSSH RSA Signature expected
                String header = new String(sig);

                if (!header.equals(getAlgorithmName())) {
                    throw new InvalidSshKeySignatureException();
                }

                sig = bar.readBinaryString();
            }

            Signature s = Signature.getInstance("SHA1withRSA");
            s.initVerify(pubKey);
            s.update(data);

            return s.verify(sig);
        } catch (NoSuchAlgorithmException nsae) {
            throw new InvalidSshKeySignatureException();
        } catch (IOException ioe) {
            throw new InvalidSshKeySignatureException();
        } catch (InvalidKeyException ike) {
            throw new InvalidSshKeySignatureException();
        } catch (SignatureException se) {
            throw new InvalidSshKeySignatureException();
        }
    }
}
