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

import org.junit.Test;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.connection.ConnectionImpl;
import net.schmizz.sshj.transport.Transport;
import net.schmizz.sshj.transport.TransportException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthenticatedHeartbeaterTest {

    private static AuthenticatedHeartbeater heartbeater(final Transport transport, final boolean authenticated) {
        final ConnectionImpl connection = mock(ConnectionImpl.class);
        when(connection.getTransport()).thenReturn(transport);
        when(transport.getConfig()).thenReturn(new DefaultConfig());
        when(transport.getService()).thenReturn(authenticated ? connection : null);
        return new AuthenticatedHeartbeater(connection);
    }

    @Test
    public void testNoPacketBeforeAuthentication() throws Exception {
        // Packet sent before authentication is processed is not compressed but server may already expect compressed
        final Transport transport = mock(Transport.class);
        heartbeater(transport, false).doKeepAlive();
        verify(transport, never()).write(any(SSHPacket.class));
    }

    @Test
    public void testPacketAfterAuthentication() throws Exception {
        final Transport transport = mock(Transport.class);
        heartbeater(transport, true).doKeepAlive();
        verify(transport, times(1)).write(any(SSHPacket.class));
    }
}
