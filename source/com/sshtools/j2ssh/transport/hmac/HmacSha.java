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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.transport.AlgorithmInitializationException;


/**
 * @author $author$
 * @version $Revision$
 */
public class HmacSha implements SshHmac {
    private static Log log = LogFactory.getLog(HmacSha.class);
    private Mac mac;

    /**
     * Creates a new HmacSha object.
     */
    public HmacSha() {
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
            mac = Mac.getInstance("HmacSha1");

            byte[] key = new byte[20];
            System.arraycopy(keydata, 0, key, 0, 20);

            SecretKeySpec keyspec = new SecretKeySpec(key, "HmacSha1");
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

        //log.debug("MAC Data length: " + String.valueOf(data.length));
        byte[] generated = generate(sequenceNo, data, 0, data.length - len);
        String compare1 = new String(generated);
        String compare2 = new String(data, data.length - len, len);

        //log.debug("Generated: " + compare1);
        //log.debug("Actual   : " + compare2);
        boolean result = compare1.equals(compare2);

        /*if (!result) {
            /**
          * Output some debug stuff
          */
        /*  String genhex = "";
            String acthex = "";
            boolean verify = true;
            for(int i=0;i<generated.length;i++) {
              genhex += (genhex.length()==0?"":",") + Integer.toHexString(generated[i] & 0xFF);
              acthex += (acthex.length()==0?"":",") + Integer.toHexString(data[data.length-len+i] & 0xFF);
              verify = (generated[i] == data[data.length-len+i]);
            }
            log.debug("Byte Verify: " + String.valueOf(verify));
            log.debug("Generated: " + genhex);
            log.debug("Actual: " + acthex);
          }*/
        return result;
    }
}
