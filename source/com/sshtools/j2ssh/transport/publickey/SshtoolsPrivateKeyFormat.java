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
package com.sshtools.j2ssh.transport.publickey;

import java.io.IOException;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.util.Hash;

/**
 *  Defines the Sshtools private key file format. This format provides optional
 *  passphrase protection and records in a standard base64 encoded format.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshtoolsPrivateKeyFormat.java,v 1.3 2002/12/10 00:07:32
 *      martianx Exp $
 */
public class SshtoolsPrivateKeyFormat
         extends Base64EncodedFileFormat
         implements SshPrivateKeyFormat {
    private static String BEGIN =
            "---- BEGIN SSHTOOLS ENCRYPTED PRIVATE KEY ----";
    private static String END = "---- END SSHTOOLS ENCRYPTED PRIVATE KEY ----";
    private int cookie = 0x52f37abe;


    /**
     *  Create a format instance
     *
     *@param  subject  The subject header
     *@param  comment  The comment header
     */
    public SshtoolsPrivateKeyFormat(String subject, String comment) {
        super(BEGIN, END);
        setHeaderValue("Subject", subject);
        setHeaderValue("Comment", comment);
    }


    /**
     *  Creates a default instance
     */
    public SshtoolsPrivateKeyFormat() {
        super(BEGIN, END);
    }


    /**
     *  Returns the format type for debugging
     *
     *@return    "Sshtools-PrivateKey-Base64Encoded"
     */
    public String getFormatType() {
        return "Sshtools-PrivateKey-" + super.getFormatType();
    }


    /**
     *  Determines if the keyblob is passphrase protected
     *
     *@param  keyblob  the key blob to parse
     *@return          <tt>true</tt> if the key is passphrase protected
     *      otherwise <tt>false</tt>
     */
    public boolean isPassphraseProtected(byte keyblob[]) {

        try {
            ByteArrayReader bar = new ByteArrayReader(keyblob);

            String type = bar.readString();

            if (type.equals("none")) {
                return false;
            }

            if (type.equals("3des-cbc")) {
                return true;
            }
        } catch (IOException ioe) {
        }

        return false;
    }


    /**
     *  Changes the passphrase of the encrypted keyblob
     *
     *@param  keyblob                  The existing key blob
     *@param  oldPassphrase            The old passphrase
     *@param  newPassphrase            The new passphrase
     *@return                          The newly encryped keyblob
     *@throws  InvalidSshKeyException  if the key is invalid or the passphrase
     *      is incorrect
     */
    public byte[] changePassphrase(byte keyblob[], String oldPassphrase,
            String newPassphrase)
             throws InvalidSshKeyException {

        byte raw[] = decryptKeyblob(keyblob, oldPassphrase);

        return encryptKeyblob(raw, newPassphrase);
    }


    /**
     *  Decrypts the keyblob
     *
     *@param  keyblob                  The encrypted keyblob
     *@param  passphrase               The passphrase
     *@return                          The unencrypted keyblob
     *@throws  InvalidSshKeyException  if the key is invalid or the passphrase
     *      is incorrect
     */
    public byte[] decryptKeyblob(byte keyblob[], String passphrase)
             throws InvalidSshKeyException {

        try {
            ByteArrayReader bar = new ByteArrayReader(keyblob);

            String type = bar.readString();
            keyblob = bar.readBinaryString();

            if (type.equals("3des-cbc")) {
                // Decrypt the key
                byte keydata[] = makePassphraseKey(passphrase);
                byte iv[] = new byte[8];

                Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
                KeySpec keyspec = new DESedeKeySpec(keydata);
                Key key =
                        SecretKeyFactory.getInstance("DESede").generateSecret(keyspec);

                cipher.init(Cipher.DECRYPT_MODE, key,
                        new IvParameterSpec(iv, 0, cipher.getBlockSize()));

                ByteArrayReader data =
                        new ByteArrayReader(cipher.doFinal(keyblob));

                if (data.readInt() == cookie) {
                    keyblob = data.readBinaryString();
                } else {
                    throw new InvalidSshKeyException("The host key is invalid, check the passphrase supplied");
                }
            }

            return keyblob;
        } catch (Exception aoe) {
            throw new InvalidSshKeyException("Failed to read host key");
        }
    }


    /**
     *  Encrypts the keyblob
     *
     *@param  keyblob     The encoded key blob
     *@param  passphrase  The passphrase
     *@return             the encrypted key blob
     */
    public byte[] encryptKeyblob(byte keyblob[], String passphrase) {
        try {
            ByteArrayWriter baw = new ByteArrayWriter();

            String type = "none";

            if (passphrase != null) {
                if (!passphrase.trim().equals("")) {
                    // Encrypt the data
                    type = "3des-cbc";

                    // Decrypt the key
                    byte keydata[] = makePassphraseKey(passphrase);
                    byte iv[] = new byte[8];

                    Cipher cipher =
                            Cipher.getInstance("DESede/CBC/PKCS5Padding");
                    KeySpec keyspec = new DESedeKeySpec(keydata);
                    Key key =
                            SecretKeyFactory.getInstance("DESede").generateSecret(keyspec);

                    cipher.init(Cipher.ENCRYPT_MODE, key,
                            new IvParameterSpec(iv, 0, cipher.getBlockSize()));

                    ByteArrayWriter data = new ByteArrayWriter();
                    data.writeInt(cookie);
                    data.writeBinaryString(keyblob);
                    keyblob = cipher.doFinal(data.toByteArray());
                }
            }

            // Write the type of encryption
            baw.writeString(type);

            // Write the key blob
            baw.writeBinaryString(keyblob);

            // Now set the keyblob to our new encrpyted (or not) blob
            return baw.toByteArray();
        } catch (Exception ioe) {
            return null;
        }
    }


    /**
     *  Determines if the algorithm is supported for this format
     *
     *@param  algorithm
     *@return            <tt>true</tt> if the algorithm is supported otherwise
     *      <tt>false</tt>
     */
    public boolean supportsAlgorithm(String algorithm) {
        return true;
    }


    /**
     *  Makes the cipher key from the passphrase
     *
     *@param  passphrase  the keys passphrase
     *@return             the key for passphrase encryption
     */
    private byte[] makePassphraseKey(String passphrase) {
        try {
            // Generate the key using the passphrase
            Hash md5 = new Hash("MD5");
            md5.putBytes(passphrase.getBytes());

            byte key1[] = md5.doFinal();

            md5.reset();
            md5.putBytes(passphrase.getBytes());
            md5.putBytes(key1);

            byte key2[] = md5.doFinal();

            // Blank iv
            byte iv[] = new byte[8];
            byte key[] = new byte[32];
            System.arraycopy(key1, 0, key, 0, 16);
            System.arraycopy(key2, 0, key, 16, 16);

            return key;
        } catch (NoSuchAlgorithmException nsae) {
            return null;
        }
    }
}
