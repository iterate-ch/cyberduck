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

import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  This class represents an RSA key for j2ssh
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshRsaPrivateKey.java,v 1.5 2002/12/10 00:07:33 martianx Exp
 *      $
 */
public class SshRsaPrivateKey
         extends SshPrivateKey {
    RSAPrivateKey prvKey;
    RSAPublicKey pubKey;


    /**
     *  Creates a new SshRsaPrivateKey object.
     *
     *@param  prv  a JCE RSA private key
     *@param  pub  the keys public component
     */
    public SshRsaPrivateKey(RSAPrivateKey prv, RSAPublicKey pub) {
        prvKey = prv;
        pubKey = pub;
    }


    /**
     *  Creates a new SshRsaPrivateKey object.
     *
     *@param  encoded                     an ssh encoded private key
     *@exception  InvalidSshKeyException  Description of the Exception
     *@throws  InvalidSshKeyException     if the encoded key is invalid
     */
    public SshRsaPrivateKey(byte encoded[])
             throws InvalidSshKeyException {
        try {
            // Extract the key information
            ByteArrayReader bar = new ByteArrayReader(encoded);

            // Read the public key
            String header = bar.readString();

            if (!header.equals(getAlgorithmName())) {
                throw new InvalidSshKeyException();
            }

            BigInteger e = bar.readBigInteger();
            BigInteger n = bar.readBigInteger();

            // Read the private key
            BigInteger p = bar.readBigInteger();

            RSAPrivateKeySpec prvSpec = new RSAPrivateKeySpec(n, p);
            RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(n, e);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            prvKey = (RSAPrivateKey) kf.generatePrivate(prvSpec);
            pubKey = (RSAPublicKey) kf.generatePublic(pubSpec);
        } catch (Exception e) {
            throw new InvalidSshKeyException();
        }
    }


    /**
     *  Gets the algorithm name
     *
     *@return    "ssh-rsa"
     */
    public String getAlgorithmName() {
        return "ssh-rsa";
    }


    /**
     *  Gets the bit length of this key
     *
     *@return    the bit length
     */
    public int getBitLength() {
        return prvKey.getModulus().bitLength();
    }


    /**
     *  Gets the ssh encoded blob for this key in the following format:<br>
     *  NOTE: Our rsa implementation records both private AND public key in the
     *  private key instance<br>
     *  <br>
     *  String "ssh-rsa" MPINT e MPINT n MPINT p
     *
     *@return    the encoded byte array
     */
    public byte[] getEncoded() {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();

            // The private key consists of the public key blob
            baw.write(getPublicKey().getEncoded());

            // And the private data
            baw.writeBigInteger(prvKey.getPrivateExponent());

            return baw.toByteArray();
        } catch (IOException ioe) {
            return null;
        }
    }


    /**
     *  Gets the public component for this private key
     *
     *@return    the public key instance
     */
    public SshPublicKey getPublicKey() {
        return new SshRsaPublicKey(pubKey);
    }


    /**
     *  Generates a signature over the data supplied in the following format:
     *  <br>
     *  <br>
     *  String "ssh-rsa"<br>
     *  String signature blob<br>
     *
     *
     *@param  data  the data to sign
     *@return       the generated signature
     */
    public byte[] generateSignature(byte data[]) {
        try {
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initSign(prvKey);
            sig.update(data);

            ByteArrayWriter baw = new ByteArrayWriter();
            baw.writeString(getAlgorithmName());
            baw.writeBinaryString(sig.sign());

            return baw.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
