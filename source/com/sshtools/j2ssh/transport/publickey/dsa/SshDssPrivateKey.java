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
package com.sshtools.j2ssh.transport.publickey.dsa;

import java.io.IOException;

import java.math.BigInteger;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;

import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  This class represents a DSA private key.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshDssPrivateKey.java,v 1.4 2002/12/10 00:07:32 martianx Exp
 *      $
 */
class SshDssPrivateKey
         extends SshPrivateKey {
    DSAPrivateKey prvkey;


    /**
     *  Creates a new SshDssPrivateKey object.
     *
     *@param  prvkey  a JCE DSA key
     */
    public SshDssPrivateKey(DSAPrivateKey prvkey) {
        this.prvkey = prvkey;
    }


    /**
     *  Creates a new SshDssPrivateKey object from the key blob in the following
     *  format:<br>
     *  <br>
     *  String "ssh-dss"<br>
     *  MPINT p<br>
     *  MPINT q<br>
     *  MPINT g<br>
     *  MPINT x<br>
     *
     *
     *@param  key                         the private key blob
     *@exception  InvalidSshKeyException  Description of the Exception
     *@throws  InvalidSshKeyException     if the key is invalid
     */
    public SshDssPrivateKey(byte key[])
             throws InvalidSshKeyException {
        try {
            DSAPrivateKeySpec dsaKey;

            // Extract the key information
            ByteArrayReader bar = new ByteArrayReader(key);

            String header = bar.readString();

            if (!header.equals(getAlgorithmName())) {
                throw new InvalidSshKeyException();
            }

            BigInteger p = bar.readBigInteger();
            BigInteger q = bar.readBigInteger();
            BigInteger g = bar.readBigInteger();
            BigInteger x = bar.readBigInteger();

            dsaKey = new DSAPrivateKeySpec(x, p, q, g);

            KeyFactory kf = KeyFactory.getInstance("DSA");
            prvkey = (DSAPrivateKey) kf.generatePrivate(dsaKey);
        } catch (Exception e) {
            throw new InvalidSshKeyException();
        }
    }


    /**
     *  Gets the SSH alogorthm name
     *
     *@return    "ssh-dss"
     */
    public String getAlgorithmName() {
        return "ssh-dss";
    }


    /**
     *  Gets the bit lengh of the key
     *
     *@return    the number of bits
     */
    public int getBitLength() {
        return prvkey.getX().bitLength();
    }


    /**
     *  Gets an encoded private key blob in the following format:<br>
     *  <br>
     *  String "ssh-dss"<br>
     *  MPINT p<br>
     *  MPINT q<br>
     *  MPINT g<br>
     *  MPINT x<br>
     *
     *
     *@return    the private key blob
     */
    public byte[] getEncoded() {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();
            baw.writeString("ssh-dss");
            baw.writeBigInteger(prvkey.getParams().getP());
            baw.writeBigInteger(prvkey.getParams().getQ());
            baw.writeBigInteger(prvkey.getParams().getG());
            baw.writeBigInteger(prvkey.getX());

            return baw.toByteArray();
        } catch (IOException ioe) {
            return null;
        }
    }


    /**
     *  Gets the public key for this private key
     *
     *@return    a public key instance
     */
    public SshPublicKey getPublicKey() {
        try {
            DSAPublicKeySpec spec =
                    new DSAPublicKeySpec(getY(), prvkey.getParams().getP(),
                    prvkey.getParams().getQ(),
                    prvkey.getParams().getG());

            KeyFactory kf = KeyFactory.getInstance("DSA");

            return new SshDssPublicKey((DSAPublicKey) kf.generatePublic(spec));
        } catch (Exception e) {
            return null;
        }
    }


    /**
     *  Generates a signature over the data supplied in the following format:
     *  <br>
     *  <br>
     *  String "ssh-dss"<br>
     *  String signature blob<br>
     *
     *
     *@param  data  the data to sign
     *@return       the signature
     */
    public byte[] generateSignature(byte data[]) {
        try {
            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initSign(prvkey);

            java.util.Random rnd = new java.util.Random();
            byte buffer[] = new byte[20];
            rnd.nextBytes(buffer);

            sig.update(buffer);

            byte test[] = sig.sign();

            sig.update(data);

            byte signature[] = sig.sign();
            byte decoded[] = new byte[40];

            // Extract the r value from the der encoded signature
            int rlen = signature[3];

            // Copy the r value across
            System.arraycopy(signature, 4 + (rlen - 20), decoded, 0, 20);

            // Extract the s value length
            int slen = signature[4 + rlen + 1];

            // Copy the s value accross adjusting for the length
            System.arraycopy(signature, 4 + rlen + 2 + (slen - 20), decoded,
                    20, 20);

            ByteArrayWriter baw = new ByteArrayWriter();
            baw.writeString(getAlgorithmName());
            baw.writeBinaryString(decoded);

            return baw.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     *  Generates the Y value of the public key from the private components
     *
     *@return    the public y value
     */
    private BigInteger getY() {
        return prvkey.getParams().getG().modPow(prvkey.getX(),
                prvkey.getParams().getP());
    }
}
