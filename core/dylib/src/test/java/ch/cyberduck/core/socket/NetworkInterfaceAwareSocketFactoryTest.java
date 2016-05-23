package ch.cyberduck.core.socket;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Arrays;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class NetworkInterfaceAwareSocketFactoryTest {

    @Test
    public void testFindWithExplicitInterfaceZoneId() throws Exception {
        final Socket socket = new NetworkInterfaceAwareSocketFactory(Arrays.<String>asList("awdl0", "utun0"))
                .createSocket(InetAddress.getByName("::1%lo0"), 22);
        assertNotNull(socket);
        assertTrue(socket.getInetAddress() instanceof Inet6Address);
        assertEquals(((Inet6Address) socket.getInetAddress()).getScopeId(),
                NetworkInterface.getByName("lo0").getIndex());
    }

    @Test
    public void testFindEn0DefaultWithInetAddress() throws Exception {
        final Socket socket = new NetworkInterfaceAwareSocketFactory(Arrays.<String>asList("awdl0", "utun0"))
                .createSocket(InetAddress.getByName("::1"), 22);
        assertNotNull(socket);
        assertTrue(socket.getInetAddress() instanceof Inet6Address);
        assertEquals(((Inet6Address) socket.getInetAddress()).getScopeId(),
                NetworkInterface.getByName("en0").getIndex());
    }

    @Test
    public void testFindEn0DefaultWithHostname() throws Exception {
        final Socket socket = new NetworkInterfaceAwareSocketFactory(Arrays.<String>asList("awdl0", "utun0"))
                .createSocket("::1", 22);
        assertNotNull(socket);
        assertTrue(socket.getInetAddress() instanceof Inet6Address);
        assertEquals(((Inet6Address) socket.getInetAddress()).getScopeId(),
                NetworkInterface.getByName("en0").getIndex());
    }

    // IPv6 in the test environment
    @Test
    @Ignore
    public void testFindEn0DefaultWithUnknownHost() throws Exception {
        final Socket socket = new NetworkInterfaceAwareSocketFactory(Arrays.<String>asList("awdl0", "utun0")).createSocket();
        assertNotNull(socket);
        assertNull(socket.getInetAddress());
        socket.connect(new InetSocketAddress("ftp6.netbsd.org", 22), 0);
        assertTrue(socket.getInetAddress() instanceof Inet6Address);
        assertEquals(((Inet6Address) socket.getInetAddress()).getScopeId(),
                NetworkInterface.getByName("en0").getIndex());
    }
}