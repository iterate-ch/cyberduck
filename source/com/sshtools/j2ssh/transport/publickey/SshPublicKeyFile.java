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
 *  Represents the Public Key as a file and allows formats to be applied for
 *  conversion.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SshPublicKeyFile.java,v 1.3 2002/12/10 00:07:32 martianx Exp
 *      $
 */
public class SshPublicKeyFile {
    private SshPublicKeyFormat format;
    private byte keyblob[];


    /**
     *  Creates a public key file instance
     *
     *@param  keyblob  the public key blob
     *@param  format   the format to apply
     */
    protected SshPublicKeyFile(byte keyblob[], SshPublicKeyFormat format) {
        this.keyblob = keyblob;
        this.format = format;
    }


    /**
     *  Returns the formatted key as a byte array
     *
     *@return    the formatted key
     */
    public byte[] getBytes() {
        return format.formatKey(keyblob);
    }


    /**
     *  Returns the key blob for the creation of an SshPublicKey
     *
     *@return    the key blob
     */
    public byte[] getKeyBlob() {
        return keyblob;
    }


    /**
     *  Create a public key file instance
     *
     *@param  key     the public key instance
     *@param  format  the format to apply
     *@return         the public key file instance
     */
    public static SshPublicKeyFile create(SshPublicKey key,
            SshPublicKeyFormat format) {
        return new SshPublicKeyFile(key.getEncoded(), format);
    }


    /**
     *  Parses the file and returns an SshPublicKeyFile instance
     *
     *@param  keyfile                  the file to open
     *@param  format                   the format to parse
     *@return                          the public key file instance
     *@throws  InvalidSshKeyException  if the key/format is invalid
     *@throws  IOException             if an IO error occurs
     */
    public static SshPublicKeyFile parse(File keyfile, SshPublicKeyFormat format)
             throws InvalidSshKeyException, IOException {
        FileInputStream in = new FileInputStream(keyfile);
        byte data[] = new byte[in.available()];
        in.read(data);
        in.close();

        return parse(data, format);
    }


    /**
     *  Parses the formatted key data and returns the file instance
     *
     *@param  formattedKey             the formatted key data
     *@param  format                   the format to parse
     *@return                          the public key file instance
     *@throws  InvalidSshKeyException  if the key/format is invalid
     */
    public static SshPublicKeyFile parse(byte formattedKey[],
            SshPublicKeyFormat format)
             throws InvalidSshKeyException {
        byte keyblob[] = format.getKeyBlob(formattedKey);

        return new SshPublicKeyFile(keyblob, format);
    }


    /**
     *  Returna the public key algorithm for this public key file
     *
     *@return    the algorithm name
     */
    public String getAlgorithm() {
        return ByteArrayReader.readString(keyblob, 0);
    }


    /**
     *  Sets the format instance for this public key file
     *
     *@param  newFormat                the new format to apply
     *@throws  InvalidSshKeyException  if the format does not support the
     *      algorithm
     */
    public void setFormat(SshPublicKeyFormat newFormat)
             throws InvalidSshKeyException {
        if (newFormat.supportsAlgorithm(getAlgorithm())) {
            newFormat.setComment(format.getComment());
            this.format = newFormat;
        } else {
            throw new InvalidSshKeyException("The format does not support the public key algorithm");
        }
    }


    /**
     *  Gets the format instance for this public key file
     *
     *@return    the current format
     */
    public SshPublicKeyFormat getFormat() {
        return format;
    }


    /**
     *  Returns an SSHPublicKey instance for this public key
     *
     *@return                          the public key
     *@throws  InvalidSshKeyException  if the key is invalid
     */
    public SshPublicKey toPublicKey()
             throws IOException {

        ByteArrayReader bar = new ByteArrayReader(keyblob);
        String type = bar.readString();
        SshKeyPair pair = SshKeyPairFactory.newInstance(type);

        return pair.decodePublicKey(keyblob);
    }


    /**
     *  Returns the formatted key as a string
     *
     *@return    the formatted key
     */
    public String toString() {
        return new String(format.formatKey(keyblob));
    }
}
