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
package com.sshtools.j2ssh.transport.publickey;

/**
 * Defines an interface for performing conversion between file format data and
 * an encoded public/private key. This is used to save and convert keys to
 * differing key file types.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public interface SshKeyFormatConversion {
    /**
     * Returns the format type for degugging purposes
     *
     * @return
     */
    public String getFormatType();

    /**
     * Takes the formatted data and outputs an encoded key blob that can be
     * read by the j2ssh public key algorithm implementations
     *
     * @param formattedKey The formatted key data
     *
     * @return the encoded key blob
     *
     * @throws InvalidSshKeyException if the formatted data is invalid
     */
    public byte[] getKeyBlob(byte formattedKey[])
                      throws InvalidSshKeyException;

    /**
     * Takes the encoded key blob and outputs formatted data. This method may
     * read the blob and may change the representation of the blob within the
     * format for its own purposes, as long conversion back by
     * <code>getKeyBlob</code> encodeds the key back to its original encoding.
     *
     * @param keyBlob The SSH encoded public key
     *
     * @return the formatted data
     */
    public byte[] formatKey(byte keyBlob[]);
}
