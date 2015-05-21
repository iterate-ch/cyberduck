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

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class NetworkInterfaceAwareSocketFactoryTest extends AbstractTestCase {

    @Test
    public void testFindEn0Default() throws Exception {
        final Socket socket = new NetworkInterfaceAwareSocketFactory().createSocket(InetAddress.getByName("::1"), 22);
        assertNotNull(socket);
        assertTrue(socket.getInetAddress() instanceof Inet6Address);
        assertEquals(((Inet6Address) socket.getInetAddress()).getScopeId(),
                NetworkInterface.getByName("en0").getIndex());
    }
}