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

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;

import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeySignatureException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

/**
 *  This class represents the DSA Public key
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id$
 */
public class SshDssPublicKey
         extends SshPublicKey {
    private DSAPublicKey pubkey;


    /**
     *  Creates a new SshDssPublicKey object.
     *
     *@param  key  a JCE DSA Public key instance
     */
    public SshDssPublicKey(DSAPublicKey key) {
        this.pubkey = key;
    }


    /**
     *  Creates a new SshDssPublicKey object from an SSH encoded key blob in the
     *  following format:<br>
     *  <br>
     *  String "ssh-dss"<br>
     *  MPINT p<br>
     *  MPINT q<br>
     *  MPINT g<br>
     *  MPINT y<br>
     *
     *
     *@param  key                         the public key blob
     *@exception  InvalidSshKeyException  Description of the Exception
     *@throws  InvalidSshKeyException     if the key is invalid
     */
    public SshDssPublicKey(byte key[])
             throws InvalidSshKeyException {
        try {
            DSAPublicKeySpec dsaKey;

            // Extract the key information
            ByteArrayReader bar = new ByteArrayReader(key);

            String header = bar.readString();

            if (!header.equals(getAlgorithmName())) {
                throw new InvalidSshKeyException();
            }

            BigInteger p = bar.readBigInteger();
            BigInteger q = bar.readBigInteger();
            BigInteger g = bar.readBigInteger();
            BigInteger y = bar.readBigInteger();

            dsaKey = new DSAPublicKeySpec(y, p, q, g);

            KeyFactory kf = KeyFactory.getInstance("DSA");
            pubkey = (DSAPublicKey) kf.generatePublic(dsaKey);
        } catch (Exception e) {
            throw new InvalidSshKeyException();
        }
    }


    /**
     *  Gets the algorithm name
     *
     *@return    "ssh-dss"
     */
    public String getAlgorithmName() {
        return "ssh-dss";
    }


    /**
     *  Gets the bit length of the public key
     *
     *@return    the bit length
     */
    public int getBitLength() {
        return pubkey.getY().bitLength();
    }


    /**
     *  Encoded the key into an SSH specification keyblob in the following
     *  format:<br>
     *  String "ssh-dss"<br>
     *  MPINT p<br>
     *  MPINT q<br>
     *  MPINT g<br>
     *  MPINT y<br>
     *
     *
     *@return    the encoded key blob
     */
    public byte[] getEncoded() {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();

            baw.writeString(getAlgorithmName());
            baw.writeBigInteger(pubkey.getParams().getP());
            baw.writeBigInteger(pubkey.getParams().getQ());
            baw.writeBigInteger(pubkey.getParams().getG());
            baw.writeBigInteger(pubkey.getY());

            return baw.toByteArray();
        } catch (IOException ioe) {
            return null;
        }
    }


    /**
     *  Verifies a SSH signature blob in the following format:<br>
     *  <br>
     *  String "ssh-dss"<br>
     *  String signature blob<br>
     *
     *
     *@param  signature                         the SSH signature blob
     *@param  data                              the data to which the signature
     *      belongs
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

            if (sig.length != 40) {
                // OpenSSH DSA Signature expected
                String header = new String(sig);

                if (!header.equals("ssh-dss")) {
                    throw new InvalidSshKeySignatureException();
                }

                sig = bar.readBinaryString();
            }

            bar = new ByteArrayReader(sig);

            byte raw[] = new byte[40];
            bar.read(raw);

            byte encoded[];

            // Determine the encoded length of the big integers
            int rlen = (((raw[0] & 0x80) == 0x80) ? 0x15 : 0x14);
            int slen = (((raw[20] & 0x80) == 0x80) ? 0x15 : 0x14);

            byte asn1r[] = {0x30, (byte) (rlen + slen + 4), 0x02, (byte) rlen};
            byte asn1s[] = {0x02, (byte) slen};

            // Create the encoded byte array
            encoded = new byte[asn1r.length + rlen + asn1s.length + slen];

            // Copy the data and encode it into the array
            System.arraycopy(asn1r, 0, encoded, 0, asn1r.length);

            // Copy the integer inserting a zero byte if signed
            int roffset = (((raw[0] & 0x80) == 0x80) ? 1 : 0);
            System.arraycopy(raw, 0, encoded, asn1r.length + roffset, 20);
            System.arraycopy(asn1s, 0, encoded, asn1r.length + roffset + 20,
                    asn1s.length);

            int soffset = (((raw[20] & 0x80) == 0x80) ? 1 : 0);
            System.arraycopy(raw, 20, encoded,
                    asn1r.length + roffset + 20 + asn1s.length
                    + soffset, 20);

            Signature s = Signature.getInstance("SHA1withDSA");
            s.initVerify(pubkey);
            s.update(data);

            return s.verify(encoded);
        } catch (NoSuchAlgorithmException nsae) {
            throw new InvalidSshKeySignatureException();
        } catch (InvalidKeyException ike) {
            throw new InvalidSshKeySignatureException();
        } catch (IOException ioe) {
            throw new InvalidSshKeySignatureException();
        } catch (SignatureException se) {
            throw new InvalidSshKeySignatureException();
        }
    }
}
