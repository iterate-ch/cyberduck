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
package com.sshtools.j2ssh.transport;

import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


/**
 * <p/>
 * An interface to allow the transport protocol to verify the public key
 * supplied by the server during key-exchange
 * </p>
 *
 * @author Lee David Painter
 * @version $Revision$
 * @since 0.2.0
 */
public interface HostKeyVerification {
    /**
     * <p/>
     * Called by the transport protocol to verify the identity of the server
     * through the supplied public key.
     * </p>
     *
     * @param host the name of the host
     * @param pk   the public key supplied during key-exchange
     * @return true if the host is acceptable, otherwise false
     * @throws TransportProtocolException if an error occurs
     * @since 0.2.0
     */
    public boolean verifyHost(String host, SshPublicKey pk)
            throws TransportProtocolException;
}
