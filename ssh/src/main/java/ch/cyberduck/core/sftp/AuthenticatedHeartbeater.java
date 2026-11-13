package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import net.schmizz.keepalive.KeepAlive;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.common.Message;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.connection.ConnectionImpl;
import net.schmizz.sshj.transport.TransportException;

/**
 * Send <code>SSH_MSG_IGNORE</code> as keep-alive but only once authentication is complete. The thread of the default
 * sshj heartbeat is started right after key exchange and sends the first packet immediately. When it is scheduled
 * after the authentication request was sent but before the client has processed <code>SSH_MSG_USERAUTH_SUCCESS</code>,
 * the packet is sent uncompressed while a server negotiating delayed compression (<code>zlib@openssh.com</code>)
 * already expects compressed packets, and drops the connection.
 */
public class AuthenticatedHeartbeater extends KeepAlive {

    public static final KeepAliveProvider PROVIDER = new KeepAliveProvider() {
        @Override
        public KeepAlive provide(final ConnectionImpl connection) {
            return new AuthenticatedHeartbeater(connection);
        }
    };

    public AuthenticatedHeartbeater(final ConnectionImpl connection) {
        super(connection, "sshj-Heartbeater");
    }

    @Override
    protected void doKeepAlive() throws TransportException {
        // Service is set to connection on authentication success, after delayed compression is put in force
        if(conn.equals(conn.getTransport().getService())) {
            conn.getTransport().write(new SSHPacket(Message.IGNORE).putString(""));
        }
    }
}
