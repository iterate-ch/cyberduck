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
package com.sshtools.j2ssh.transport.hmac;

import com.sshtools.j2ssh.transport.AlgorithmInitializationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 * @author $author$
 * @version $Revision$
 */
public class HmacMd5 implements SshHmac {
    private Mac mac;

    /**
     * Creates a new HmacMd5 object.
     */
    public HmacMd5() {
    }

    /**
     * @return
     */
    public int getMacLength() {
        return mac.getMacLength();
    }

    /**
     * @param sequenceNo
     * @param data
     * @param offset
     * @param len
     * @return
     */
    public byte[] generate(long sequenceNo, byte[] data, int offset, int len) {
        // Write the sequence no
        byte[] sequenceBytes = new byte[4];
        sequenceBytes[0] = (byte) (sequenceNo >> 24);
        sequenceBytes[1] = (byte) (sequenceNo >> 16);
        sequenceBytes[2] = (byte) (sequenceNo >> 8);
        sequenceBytes[3] = (byte) (sequenceNo >> 0);
        mac.update(sequenceBytes);
        mac.update(data, offset, len);

        return mac.doFinal();
    }

    /**
     * @param keydata
     * @throws AlgorithmInitializationException
     *
     */
    public void init(byte[] keydata) throws AlgorithmInitializationException {
        try {
            mac = Mac.getInstance("HmacMD5");

            // Create a key of 16 bytes
            byte[] key = new byte[16];
            System.arraycopy(keydata, 0, key, 0, key.length);

            SecretKeySpec keyspec = new SecretKeySpec(key, "HmacMD5");
            mac.init(keyspec);
        }
        catch (NoSuchAlgorithmException nsae) {
            throw new AlgorithmInitializationException("No provider exists for the HmacSha1 algorithm");
        }
        catch (InvalidKeyException ike) {
            throw new AlgorithmInitializationException("Invalid key");
        }
    }

    /**
     * @param sequenceNo
     * @param data
     * @return
     */
    public boolean verify(long sequenceNo, byte[] data) {
        int len = getMacLength();
        byte[] generated = generate(sequenceNo, data, 0, data.length - len);
        String compare1 = new String(generated);
        String compare2 = new String(data, data.length - len, len);

        return compare1.equals(compare2);
    }
}
