package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import static org.junit.Assert.assertNotNull;

public class ProxySocketFactoryTest extends AbstractTestCase {

    @Test
    public void testCreateSocketNoProxy() throws Exception {
        assertNotNull(new ProxySocketFactory(ProtocolFactory.FTP, new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "localhost";
            }
        }).createSocket());
    }

    @Test(expected = SocketException.class)
    public void testCreateSocketWithProxy() throws Exception {
        final Socket socket = new ProxySocketFactory(ProtocolFactory.FTP, new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "test.cyberduck.ch";
            }
        }, new DefaultSocketConfigurator(),
                new ProxyFinder() {
                    @Override
                    public boolean usePassiveFTP() {
                        return true;
                    }

                    @Override
                    public Proxy find(final Host target) {
                        return new Proxy(Proxy.Type.SOCKS, "localhost", 7000);
                    }
                }).createSocket();
        assertNotNull(socket);
        socket.connect(new InetSocketAddress("test.cyberduck.ch", 21));
    }
}
