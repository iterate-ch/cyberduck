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
package com.sshtools.j2ssh.transport.publickey;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.util.Hash;

import java.io.IOException;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class SshtoolsPrivateKeyFormat extends Base64EncodedFileFormat
    implements SshPrivateKeyFormat {
    private static String BEGIN = "---- BEGIN SSHTOOLS ENCRYPTED PRIVATE KEY ----";
    private static String END = "---- END SSHTOOLS ENCRYPTED PRIVATE KEY ----";
    private int cookie = 0x52f37abe;

    /**
     * Creates a new SshtoolsPrivateKeyFormat object.
     *
     * @param subject
     * @param comment
     */
    public SshtoolsPrivateKeyFormat(String subject, String comment) {
        super(BEGIN, END);
        setHeaderValue("Subject", subject);
        setHeaderValue("Comment", comment);
    }

    /**
     * Creates a new SshtoolsPrivateKeyFormat object.
     */
    public SshtoolsPrivateKeyFormat() {
        super(BEGIN, END);
    }

    /**
     *
     *
     * @return
     */
    public String getFormatType() {
        return "SSHTools-PrivateKey-" + super.getFormatType();
    }

    /**
     *
     *
     * @param formattedKey
     *
     * @return
     */
    public boolean isPassphraseProtected(byte[] formattedKey) {
        try {
            ByteArrayReader bar = new ByteArrayReader(getKeyBlob(formattedKey));
            String type = bar.readString();

            if (type.equals("none")) {
                return false;
            }

            if (type.equalsIgnoreCase("3des-cbc")) {
                return true;
            }
        } catch (IOException ioe) {
        }

        return false;
    }

    /**
     *
     *
     * @param formattedKey
     * @param passphrase
     *
     * @return
     *
     * @throws InvalidSshKeyException
     */
    public byte[] decryptKeyblob(byte[] formattedKey, String passphrase)
        throws InvalidSshKeyException {
        try {
            byte[] keyblob = getKeyBlob(formattedKey);
            ByteArrayReader bar = new ByteArrayReader(keyblob);
            String type = bar.readString();

            if (type.equalsIgnoreCase("3des-cbc")) {
                // Decrypt the key
                byte[] keydata = makePassphraseKey(passphrase);
                byte[] iv = new byte[8];

                if (type.equals("3DES-CBC")) {
                    bar.read(iv);
                }

                keyblob = bar.readBinaryString();

                Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
                KeySpec keyspec = new DESedeKeySpec(keydata);
                Key key = SecretKeyFactory.getInstance("DESede").generateSecret(keyspec);
                cipher.init(Cipher.DECRYPT_MODE, key,
                    new IvParameterSpec(iv, 0, cipher.getBlockSize()));

                ByteArrayReader data = new ByteArrayReader(cipher.doFinal(
                            keyblob));

                if (data.readInt() == cookie) {
                    keyblob = data.readBinaryString();
                } else {
                    throw new InvalidSshKeyException(
                        "The host key is invalid, check the passphrase supplied");
                }
            } else {
                keyblob = bar.readBinaryString();
            }

            return keyblob;
        } catch (Exception aoe) {
            throw new InvalidSshKeyException("Failed to read host key");
        }
    }

    /**
     *
     *
     * @param keyblob
     * @param passphrase
     *
     * @return
     */
    public byte[] encryptKeyblob(byte[] keyblob, String passphrase) {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();
            String type = "none";

            if (passphrase != null) {
                if (!passphrase.trim().equals("")) {
                    // Encrypt the data
                    type = "3DES-CBC";

                    // Decrypt the key
                    byte[] keydata = makePassphraseKey(passphrase);
                    byte[] iv = new byte[8];
                    ConfigurationLoader.getRND().nextBytes(iv);

                    Cipher cipher = Cipher.getInstance(
                            "DESede/CBC/PKCS5Padding");
                    KeySpec keyspec = new DESedeKeySpec(keydata);
                    Key key = SecretKeyFactory.getInstance("DESede")
                                              .generateSecret(keyspec);
                    cipher.init(Cipher.ENCRYPT_MODE, key,
                        new IvParameterSpec(iv, 0, cipher.getBlockSize()));

                    ByteArrayWriter data = new ByteArrayWriter();
                    baw.writeString(type);
                    baw.write(iv);
                    data.writeInt(cookie);
                    data.writeBinaryString(keyblob);

                    // Encrypt and write
                    baw.writeBinaryString(cipher.doFinal(data.toByteArray()));

                    return formatKey(baw.toByteArray());
                }
            }

            // Write the type of encryption
            baw.writeString(type);

            // Write the key blob
            baw.writeBinaryString(keyblob);

            // Now set the keyblob to our new encrpyted (or not) blob
            return formatKey(baw.toByteArray());
        } catch (Exception ioe) {
            return null;
        }
    }

    /**
     *
     *
     * @param algorithm
     *
     * @return
     */
    public boolean supportsAlgorithm(String algorithm) {
        return true;
    }

    private byte[] makePassphraseKey(String passphrase) {
        try {
            // Generate the key using the passphrase
            Hash md5 = new Hash("MD5");
            md5.putBytes(passphrase.getBytes());

            byte[] key1 = md5.doFinal();
            md5.reset();
            md5.putBytes(passphrase.getBytes());
            md5.putBytes(key1);

            byte[] key2 = md5.doFinal();
            byte[] key = new byte[32];
            System.arraycopy(key1, 0, key, 0, 16);
            System.arraycopy(key2, 0, key, 16, 16);

            return key;
        } catch (NoSuchAlgorithmException nsae) {
            return null;
        }
    }
}
