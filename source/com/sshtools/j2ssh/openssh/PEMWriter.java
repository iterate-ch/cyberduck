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
package com.sshtools.j2ssh.openssh;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.util.Base64;


/**
 * @author $author$
 * @version $Revision$
 */
public class PEMWriter extends PEM {
    private String type;
    private Map header = new HashMap();
    private byte[] payload;

    /**
     * Creates a new PEMWriter object.
     */
    public PEMWriter() {
    }

    /**
     * @param w
     * @throws IOException
     */
    public void write(Writer w) throws IOException {
        PrintWriter writer = new PrintWriter(w, true);
        writer.println(PEM_BEGIN + type + PEM_BOUNDARY);

        if (!header.isEmpty()) {
            for (Iterator i = header.keySet().iterator(); i.hasNext();) {
                String key = (String)i.next();
                String value = (String)header.get(key);
                writer.print(key + ": ");

                if ((key.length() + value.length() + 2) > MAX_LINE_LENGTH) {
                    int offset = Math.max(MAX_LINE_LENGTH - key.length() - 2, 0);
                    writer.println(value.substring(0, offset) + "\\");

                    for (; offset < value.length();
                         offset += MAX_LINE_LENGTH) {
                        if ((offset + MAX_LINE_LENGTH) >= value.length()) {
                            writer.println(value.substring(offset));
                        }
                        else {
                            writer.println(value.substring(offset,
                                    offset + MAX_LINE_LENGTH) + "\\");
                        }
                    }
                }
                else {
                    writer.println(value);
                }
            }

            writer.println();
        }

        writer.println(Base64.encodeBytes(payload, false));
        writer.println(PEM_END + type + PEM_BOUNDARY);
    }

    /**
     * @param payload
     * @param passphrase
     * @throws GeneralSecurityException
     */
    public void encryptPayload(byte[] payload, String passphrase)
            throws GeneralSecurityException {
        if ((passphrase == null) || (passphrase.length() == 0)) {
            // Simple case: no passphrase means no encryption of the private key
            setPayload(payload);

            return;
        }

        SecureRandom rnd = ConfigurationLoader.getRND();
        byte[] iv = new byte[8];
        rnd.nextBytes(iv);

        StringBuffer ivString = new StringBuffer(16);

        for (int i = 0; i < iv.length; i++) {
            ivString.append(HEX_CHARS[(iv[i] & 0xff) >> 4]);
            ivString.append(HEX_CHARS[iv[i] & 0x0f]);
        }

        header.put("DEK-Info", "DES-EDE3-CBC," + ivString);
        header.put("Proc-Type", "4,ENCRYPTED");

        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        SecretKey key = getKeyFromPassphrase(passphrase, iv, 24);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] encrypted = new byte[payload.length];
        cipher.update(payload, 0, payload.length, encrypted, 0);
        setPayload(encrypted);
    }

    /**
     * @return
     */
    public Map getHeader() {
        return header;
    }

    /**
     * @return
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * @param bs
     */
    public void setPayload(byte[] bs) {
        payload = bs;
    }

    /**
     * @param string
     */
    public void setType(String string) {
        type = string;
    }
}
