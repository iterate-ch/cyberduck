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

import java.util.StringTokenizer;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.util.Base64;

/**
 *  Defines the OpenSSH public key file format for the Java SSH API
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: OpenSSHPublicKeyFormat.java,v 1.3 2002/12/10 00:07:32
 *      martianx Exp $
 */
public class OpenSSHPublicKeyFormat
         implements SshPublicKeyFormat {
    String comment = null;


    /**
     *  Creates a format instance
     *
     *@param  comment  The comment to apply
     */
    public OpenSSHPublicKeyFormat(String comment) {
        setComment(comment);
    }


    /**
     *  Creates a format instance
     */
    public OpenSSHPublicKeyFormat() { }


    /**
     *  Sets the comment
     *
     *@param  comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }


    /**
     *  Gets the comment
     *
     *@return    the comment string
     */
    public String getComment() {
        return comment;
    }


    /**
     *  Returns the format name for debugging
     *
     *@return    "OpenSSH-PublicKey"
     */
    public String getFormatType() {
        return "OpenSSH-PublicKey";
    }


    /**
     *  Unformats the key into an SSH encoded key
     *
     *@param  formattedKey             The OpenSSH formatted key
     *@return                          the ssh encoded key blob
     *@throws  InvalidSshKeyException  if the formatted data is an invalid key
     */
    public byte[] getKeyBlob(byte formattedKey[])
             throws InvalidSshKeyException {
        StringTokenizer split =
                new StringTokenizer(new String(formattedKey), " ");
        String algorithm = (String) split.nextElement();
        String encoded = (String) split.nextElement();
        comment = (String) split.nextElement();

        return Base64.decode(encoded);
    }


    /**
     *  Formats the key from an Ssh encoded key as described in [SSH-TRANS] to
     *  an OpenSSH formatted key
     *
     *@param  keyblob  The ssh encoded key
     *@return          the OpenSSh formatted key
     */
    public byte[] formatKey(byte keyblob[]) {
        String algorithm = ByteArrayReader.readString(keyblob, 0);

        String formatted = algorithm + " " + Base64.encodeBytes(keyblob, true);

        if (comment != null) {
            formatted += (" " + comment + "\n");
        }

        return formatted.getBytes();
    }


    /**
     *  Evaluates whether the format can format the given public key algorithm
     *
     *@param  algorithm
     *@return            <tt>true</tt> id the format supports the algorithm
     *      otherwise <tt>false</tt>
     */
    public boolean supportsAlgorithm(String algorithm) {
        return true;
    }
}
