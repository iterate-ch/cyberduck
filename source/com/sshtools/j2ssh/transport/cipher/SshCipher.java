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
package com.sshtools.j2ssh.transport.cipher;

import com.sshtools.j2ssh.transport.AlgorithmOperationException;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public abstract class SshCipher {
    /**  */
    public static final int ENCRYPT_MODE = 0;

    /**  */
    public static final int DECRYPT_MODE = 1;

    /**
     *
     *
     * @return
     */
    public abstract int getBlockSize();

    /**
     *
     *
     * @param mode
     * @param iv
     * @param keydata
     *
     * @throws AlgorithmOperationException
     */
    public abstract void init(int mode, byte[] iv, byte[] keydata)
        throws AlgorithmOperationException;

    /**
     *
     *
     * @param data
     *
     * @return
     *
     * @throws AlgorithmOperationException
     */
    public byte[] transform(byte[] data) throws AlgorithmOperationException {
        return transform(data, 0, data.length);
    }

    /**
     *
     *
     * @param data
     * @param offset
     * @param len
     *
     * @return
     *
     * @throws AlgorithmOperationException
     */
    public abstract byte[] transform(byte[] data, int offset, int len)
        throws AlgorithmOperationException;
}
