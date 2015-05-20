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

import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testCreateSocketIPv6Localhost() throws Exception {
        final Socket socket = new ProxySocketFactory(ProtocolFactory.SFTP, new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "localhost";
            }
        }).createSocket("::1", 22);
        assertNotNull(socket);
        assertTrue(socket.getInetAddress() instanceof Inet6Address);
    }

    @Test
    public void testConnectIPv6LocalAddress() throws Exception {
        for(String address : Arrays.asList("fe80::c62c:3ff:fe0b:8670%en0")) {
            final Socket socket = new ProxySocketFactory(ProtocolFactory.SFTP, new TrustManagerHostnameCallback() {
                @Override
                public String getTarget() {
                    return "localhost";
                }
            }).createSocket(address, 22);
            assertNotNull(socket);
            assertTrue(socket.getInetAddress() instanceof Inet6Address);
        }
    }

    @Test(expected = ConnectException.class)
    public void testCreateSocketIPv6LocalAddressConnectionRefused() throws Exception {
        for(String address : Arrays.asList("fe80::9272:40ff:fe02:c363%en0")) {
            final Socket socket = new ProxySocketFactory(ProtocolFactory.SFTP, new TrustManagerHostnameCallback() {
                @Override
                public String getTarget() {
                    return "localhost";
                }
            }).createSocket(address, 22);
        }
    }

    @Test
    public void testCreateSocketDualStackGoogle() throws Exception {
        final Socket socket = new ProxySocketFactory(ProtocolFactory.WEBDAV, new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "localhost";
            }
        }).createSocket("ipv6test.google.com", 80);
        assertNotNull(socket);
        // We have set `java.net.preferIPv6Addresses` to `false` by default
        assertTrue(socket.getInetAddress() instanceof Inet4Address);
    }

    @Test
    public void testCreateSocketIPv6Only() throws Exception {
        for(String address : Arrays.asList("ftp6.netbsd.org")) {
            final Socket socket = new ProxySocketFactory(ProtocolFactory.SFTP, new TrustManagerHostnameCallback() {
                @Override
                public String getTarget() {
                    return "ftp6.netbsd.org";
                }
            }).createSocket(address, 22);
            assertNotNull(socket);
            assertTrue(socket.getInetAddress() instanceof Inet6Address);
        }
    }
}