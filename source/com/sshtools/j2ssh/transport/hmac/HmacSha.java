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
package com.sshtools.j2ssh.transport.hmac;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sshtools.j2ssh.transport.AlgorithmInitializationException;


/**
 * Implements the SHA Hash Message authentication algorithm
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class HmacSha
    implements SshHmac {
    private Mac mac;

    /**
     * Constructor for the HmacSha object
     */
    public HmacSha() {
    }

    /**
     * Gets the mac length
     *
     * @return The mac length
     */
    public int getMacLength() {
        return mac.getMacLength();
    }

    /**
     * Generates the mac
     *
     * @param sequenceNo The sequence no of the message
     * @param data The message data
     * @param offset Description of the Parameter
     * @param len Description of the Parameter
     *
     * @return The mac
     */
    public byte[] generate(long sequenceNo, byte data[], int offset, int len) {
        // Write the sequence no
        byte sequenceBytes[] = new byte[4];
        sequenceBytes[0] = (byte) (sequenceNo >> 24);
        sequenceBytes[1] = (byte) (sequenceNo >> 16);
        sequenceBytes[2] = (byte) (sequenceNo >> 8);
        sequenceBytes[3] = (byte) (sequenceNo >> 0);

        mac.update(sequenceBytes);

        mac.update(data, offset, len);

        return mac.doFinal();
    }

    /**
     * Initates the message authentication with the supplied key data
     *
     * @param keydata Key data produced during key exchange
     *
     * @exception AlgorithmInitializationException if the algorithm fails to
     *            initialize
     */
    public void init(byte keydata[])
              throws AlgorithmInitializationException {
        try {
            mac = Mac.getInstance("HmacSha1");

            byte key[] = new byte[20];
            System.arraycopy(keydata, 0, key, 0, 20);

            SecretKeySpec keyspec = new SecretKeySpec(key, "HmacSha1");

            mac.init(keyspec);
        } catch (NoSuchAlgorithmException nsae) {
            throw new AlgorithmInitializationException("No provider exists for the HmacSha1 algorithm");
        } catch (InvalidKeyException ike) {
            throw new AlgorithmInitializationException("Invalid key");
        }
    }

    /**
     * Verifies the mac received
     *
     * @param sequenceNo The sequence no of the message
     * @param data The message data
     *
     * @return The result of the verification
     */
    public boolean verify(long sequenceNo, byte data[]) {
        int len = getMacLength();

        byte generated[] = generate(sequenceNo, data, 0, data.length - len);

        String compare1 = new String(generated);
        String compare2 = new String(data, data.length - len, len);

        return compare1.equals(compare2);
    }
}
