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
package com.sshtools.j2ssh.transport.publickey.dsa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeySignatureException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.util.SimpleASNWriter;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshDssPublicKey extends SshPublicKey {
    private static Log log = LogFactory.getLog(SshDssPublicKey.class);
    private DSAPublicKey pubkey;

    /**
     * Creates a new SshDssPublicKey object.
     *
     * @param key
     */
    public SshDssPublicKey(DSAPublicKey key) {
        this.pubkey = key;
    }

    /**
     * Creates a new SshDssPublicKey object.
     *
     * @param key
     * @throws InvalidSshKeyException
     */
    public SshDssPublicKey(byte[] key) throws InvalidSshKeyException {
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
            pubkey = (DSAPublicKey)kf.generatePublic(dsaKey);
        }
        catch (Exception e) {
            throw new InvalidSshKeyException();
        }
    }

    /**
     * @return
     */
    public String getAlgorithmName() {
        return "ssh-dss";
    }

    /**
     * @return
     */
    public int getBitLength() {
        return pubkey.getY().bitLength();
    }

    /**
     * @return
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
        }
        catch (IOException ioe) {
            return null;
        }
    }

    /**
     * @param signature
     * @param data
     * @return
     * @throws InvalidSshKeySignatureException
     *
     */
    public boolean verifySignature(byte[] signature, byte[] data)
            throws InvalidSshKeySignatureException {
        try {
            // Check for differing version of the transport protocol
            if (signature.length != 40) {
                ByteArrayReader bar = new ByteArrayReader(signature);
                byte[] sig = bar.readBinaryString();

                //log.debug("Signature blob is " + new String(sig));
                String header = new String(sig);
                log.debug("Header is " + header);

                if (!header.equals("ssh-dss")) {
                    throw new InvalidSshKeySignatureException();
                }

                signature = bar.readBinaryString();

                //log.debug("Read signature from blob: " + new String(signature));
            }

            // Using a SimpleASNWriter
            ByteArrayOutputStream r = new ByteArrayOutputStream();
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            SimpleASNWriter asn = new SimpleASNWriter();
            asn.writeByte(0x02);

            if (((signature[0] & 0x80) == 0x80) && (signature[0] != 0x00)) {
                r.write(0);
                r.write(signature, 0, 20);
            }
            else {
                r.write(signature, 0, 20);
            }

            asn.writeData(r.toByteArray());
            asn.writeByte(0x02);

            if (((signature[20] & 0x80) == 0x80) && (signature[20] != 0x00)) {
                s.write(0);
                s.write(signature, 20, 20);
            }
            else {
                s.write(signature, 20, 20);
            }

            asn.writeData(s.toByteArray());

            SimpleASNWriter asnEncoded = new SimpleASNWriter();
            asnEncoded.writeByte(0x30);
            asnEncoded.writeData(asn.toByteArray());

            byte[] encoded = asnEncoded.toByteArray();

            if (log.isDebugEnabled()) {
                log.debug("Verifying host key signature");
                log.debug("Signature length is " +
                        String.valueOf(signature.length));

                String hex = "";

                for (int i = 0; i < signature.length; i++) {
                    hex += (Integer.toHexString(signature[i] & 0xFF) + " ");
                }

                log.debug("SSH: " + hex);
                hex = "";

                for (int i = 0; i < encoded.length; i++) {
                    hex += (Integer.toHexString(encoded[i] & 0xFF) + " ");
                }

                log.debug("Encoded: " + hex);
            }

            // The previous way

            /*byte[] encoded;
                         // Determine the encoded length of the big integers
                         int rlen = (((signature[0] & 0x80) == 0x80) ? 0x15 : 0x14);
                         log.debug("rlen=" + String.valueOf(rlen));
                         int slen = (((signature[20] & 0x80) == 0x80) ? 0x15 : 0x14);
                         log.debug("slen=" + String.valueOf(slen));
                 byte[] asn1r = { 0x30, (byte) (rlen + slen + 4), 0x02, (byte) rlen };
                         byte[] asn1s = { 0x02, (byte) slen };
                         // Create the encoded byte array
                 encoded = new byte[asn1r.length + rlen + asn1s.length + slen];
                         // Copy the data and encode it into the array
                         System.arraycopy(asn1r, 0, encoded, 0, asn1r.length);
                         // Copy the integer inserting a zero byte if signed
                         int roffset = (((signature[0] & 0x80) == 0x80) ? 1 : 0);
                 System.arraycopy(signature, 0, encoded, asn1r.length + roffset, 20);
                 System.arraycopy(asn1s, 0, encoded, asn1r.length + roffset + 20,
                asn1s.length);
                         int soffset = (((signature[20] & 0x80) == 0x80) ? 1 : 0);
                         System.arraycopy(signature, 20, encoded,
                asn1r.length + roffset + 20 + asn1s.length + soffset, 20);
             */
            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initVerify(pubkey);
            sig.update(data);

            return sig.verify(encoded);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new InvalidSshKeySignatureException();
        }
        catch (InvalidKeyException ike) {
            throw new InvalidSshKeySignatureException();
        }
        catch (IOException ioe) {
            throw new InvalidSshKeySignatureException();
        }
        catch (SignatureException se) {
            throw new InvalidSshKeySignatureException();
        }
    }
}
