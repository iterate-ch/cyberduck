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
package com.sshtools.j2ssh.transport.hmac;

import com.sshtools.j2ssh.transport.AlgorithmInitializationException;


/**
 * This interface defines the SSH messase authentication methods
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public interface SshHmac {
    /**
     * Gets the mac length
     *
     * @return The mac length
     */
    public int getMacLength();

    /**
     * Called to generate a mac
     *
     * @param sequenceNo The sequence no of the message
     * @param data The message data
     * @param offset Description of the Parameter
     * @param len Description of the Parameter
     *
     * @return The mac
     */
    public byte[] generate(long sequenceNo, byte data[], int offset, int len);

    /**
     * Called by the framework to initialize the mac
     *
     * @param keydata Key data produced during key exchange
     *
     * @exception AlgorithmInitializationException if the algorithm fails to
     *            initialize
     */
    public void init(byte keydata[])
              throws AlgorithmInitializationException;

    /**
     * Called to verify a mac
     *
     * @param sequenceNo The sequence no of the message
     * @param data The message data
     *
     * @return The result of the verification
     */
    public boolean verify(long sequenceNo, byte data[]);
}
