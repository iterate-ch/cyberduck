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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;
import com.sshtools.j2ssh.io.ByteArrayReader;

/**
 *  Represents a Private Key file and allows different formats to be applied to
 *  the key data. To define a new format extend <code>SshPrivateKeyFormat</code>
 *  and pass in the constructor when creating the file or <code>setFormat</code>
 *  to covert the file into the new format.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshPrivateKeyFile.java,v 1.4 2002/12/10 00:07:32 martianx Exp
 *      $
 */
public class SshPrivateKeyFile {
    private SshPrivateKeyFormat format;
    private byte keyblob[];


    /**
     *  Creates a private key file instance
     *
     *@param  keyblob  the key blob for this file
     *@param  format   the format to apply
     */
    protected SshPrivateKeyFile(byte keyblob[], SshPrivateKeyFormat format) {
        this.keyblob = keyblob;
        this.format = format;
    }


    /**
     *  Returns the formatted key as a byte array
     *
     *@return    the formatted byte array
     */
    public byte[] getBytes() {
        return format.formatKey(keyblob);
    }


    /**
     *  Returns the key blob for the creation of an SshPrivateKey
     *
     *@param  passphrase               the passphrase that encrypts the key
     *@return                          the encoded key blob
     *@throws  InvalidSshKeyException  if the key is invalid
     */
    public byte[] getKeyBlob(String passphrase)
             throws InvalidSshKeyException {
        return format.decryptKeyblob(keyblob, passphrase);
    }


    /**
     *  Parses the file and returns an SshPrivateKeyFile instance
     *
     *@param  formattedKey             an array of formatted data
     *@param  format                   the format to parse
     *@return                          a private key file instance containing
     *      the key blob and allowing conversion to other formats
     *@throws  InvalidSshKeyException  if the key is invalid
     */
    public static SshPrivateKeyFile parse(byte formattedKey[],
            SshPrivateKeyFormat format)
             throws InvalidSshKeyException {
        byte keyblob[] = format.getKeyBlob(formattedKey);

        return new SshPrivateKeyFile(keyblob, format);
    }


    /**
     *  Parses the file and returns an SshPrivateKeyFile instance
     *
     *@param  keyfile                  the file to open
     *@param  format                   the format to parse
     *@return                          the private key file instance
     *@throws  InvalidSshKeyException  if the key is invalid
     *@throws  IOException             if an IO error occurs
     */
    public static SshPrivateKeyFile parse(File keyfile,
            SshPrivateKeyFormat format)
             throws InvalidSshKeyException, IOException {
        FileInputStream in = new FileInputStream(keyfile);
        byte data[] = new byte[in.available()];
        in.read(data);
        in.close();

        return parse(data, format);
    }


    /**
     *  Determines if the Private Key is protected by a passphrase
     *
     *@return    <tt>true</tt> if the file is protected otherwise <tt>false</tt>
     */
    public boolean isPassphraseProtected() {
        return format.isPassphraseProtected(keyblob);
    }


    /**
     *  Changes the passphrase
     *
     *@param  oldPassphrase            The old passphrase
     *@param  newPassphrase            The new passphrase
     *@throws  InvalidSshKeyException  if the key is invalid or passphrase is
     *      incorrect
     */
    public void changePassphrase(String oldPassphrase, String newPassphrase)
             throws InvalidSshKeyException {
        keyblob =
                format.changePassphrase(keyblob, oldPassphrase, newPassphrase);
    }


    /**
     *  Creates a Private Key file representation
     *
     *@param  key                      The SshPrivateKey
     *@param  passphrase               The passphrase to use (empty==null ||
     *      empty=="")
     *@param  format                   The format to apply
     *@return                          the private key instance
     *@throws  InvalidSshKeyException  if the key is invalid
     */
    public static SshPrivateKeyFile create(SshPrivateKey key,
            String passphrase,
            SshPrivateKeyFormat format)
             throws InvalidSshKeyException {
        byte keyblob[] = format.encryptKeyblob(key.getEncoded(), passphrase);

        return new SshPrivateKeyFile(keyblob, format);
    }


    /**
     *  Sets the format for this file
     *
     *@param  newFormat                The new format
     *@param  passphrase               The current passphrase
     *@throws  InvalidSshKeyException  if the key is invalid
     */
    public void setFormat(SshPrivateKeyFormat newFormat, String passphrase)
             throws InvalidSshKeyException {
        byte raw[] = this.format.decryptKeyblob(keyblob, passphrase);
        format = newFormat;
        keyblob = format.encryptKeyblob(raw, passphrase);
    }


    /**
     *  Gets the format instance for this public key file. This provides a means
     *  of changing format features such as headers and comments.
     *
     *@return    the current format
     */
    public SshPrivateKeyFormat getFormat() {
        return format;
    }


    /**
     *  Returns a Private Key instance
     *
     *@param  passphrase               the passphrase of the key
     *@return                          a private key instance ready for use in
     *      j2ssh
     *@throws  InvalidSshKeyException  if the key is invalid or passphrase
     *      incorrect
     */
    public SshPrivateKey toPrivateKey(String passphrase)
             throws InvalidSshKeyException {
        try {
            byte raw[] = format.decryptKeyblob(keyblob, passphrase);
            SshKeyPair pair = SshKeyPairFactory.newInstance(getAlgorithm(raw));

            return pair.decodePrivateKey(raw);
        } catch (AlgorithmNotSupportedException anse) {
            throw new InvalidSshKeyException("The public key algorithm for this private key is not supported");
        }
    }


    /**
     *  Returns the formatted key as a string
     *
     *@return    the formatted key
     */
    public String toString() {
        return new String(format.formatKey(keyblob));
    }


    /**
     *  Return the public key algorithm for this private key file
     *
     *@param  raw  the unencrypted key blob
     *@return      the algorithm name
     */
    private String getAlgorithm(byte raw[]) {
        return ByteArrayReader.readString(raw, 0);
    }
}
