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
package com.sshtools.j2ssh.transport.cipher;

import com.sshtools.j2ssh.transport.AlgorithmOperationException;


/**
 * Defines an abstract class for all SSH ciphers. Any cipher class that extends
 * this class can be used for encryption within the SSH protocol. Third party
 * implementations can dynamically add new ciphers to existing J2SSH
 * installations by inserting a CipherAlgorithm element into the SSH API
 * configuration file sshtools.xml.
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class SshCipher {
    /** Declares the cipher as encrypting */
    public static final int ENCRYPT_MODE = 0;

    /** Declares the cipher as decrypting */
    public static final int DECRYPT_MODE = 1;

    /**
     * Gets the blockSize of the cipher
     *
     * @return The blockSize value
     */
    public abstract int getBlockSize();

    /**
     * Called to initiate the cipher after construction.
     *
     * @param mode The cipher mode ENCRYPT_MODE | DECRYPT_MODE
     * @param iv The iv data output from key exchange
     * @param keydata The key data output from key exchange
     *
     * @exception AlgorithmOperationException if an algorithm operation fails
     */
    public abstract void init(int mode, byte iv[], byte keydata[])
                       throws AlgorithmOperationException;

    /**
     * Called to transform the supplied data according to the cipher mode.
     *
     * @param data The untransformed data
     *
     * @return The transformed data
     *
     * @exception AlgorithmOperationException if an algorithm operation fails
     */
    public byte[] transform(byte data[])
                     throws AlgorithmOperationException {
        return transform(data, 0, data.length);
    }

    /**
     * Called to transform the supplied data according to the cipher mode.
     *
     * @param data The untransformed data
     * @param offset The offset to start in the data
     * @param len The length to transform
     *
     * @return The transformed data
     *
     * @throws AlgorithmOperationException if an algorithm operation fails
     */
    public abstract byte[] transform(byte data[], int offset, int len)
                              throws AlgorithmOperationException;
}
