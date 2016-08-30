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
import ch.cyberduck.core.socket.DefaultSocketConfigurator;
import ch.cyberduck.core.socket.SocketConfigurator;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class ProxySocketFactoryTest {

    @Test
    public void testCreateSocketNoProxy() throws Exception {
        assertNotNull(new ProxySocketFactory(new TestProtocol(), new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "localhost";
            }
        }).createSocket("localhost", 22));
    }

    @Test(expected = SocketException.class)
    public void testCreateSocketWithProxy() throws Exception {
        final Socket socket = new ProxySocketFactory(new TestProtocol(), new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "test.cyberduck.ch";
            }
        }, new DefaultSocketConfigurator(),
                new ProxyFinder() {
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
        final Socket socket = new ProxySocketFactory(new TestProtocol(), new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "localhost";
            }
        }).withBlacklistedNetworkInterfaces(Arrays.asList("awdl0")).createSocket("::1", 22);
        assertNotNull(socket);
        assertTrue(socket.getInetAddress() instanceof Inet6Address);
    }

    // IPv6 in the test environment
    @Test
    @Ignore
    public void testConnectIPv6LocalAddress() throws Exception {
        for(String address : Arrays.asList("fe80::c62c:3ff:fe0b:8670")) {
            final Socket socket = new ProxySocketFactory(new TestProtocol(), new TrustManagerHostnameCallback() {
                @Override
                public String getTarget() {
                    return "localhost";
                }
            }).withBlacklistedNetworkInterfaces(Arrays.asList("awdl0")).createSocket(address, 22);
            assertNotNull(socket);
            assertTrue(socket.getInetAddress() instanceof Inet6Address);
        }
    }

    @Test(expected = SocketException.class)
    public void testCreateSocketIPv6LocalAddressConnectionRefused() throws Exception {
        for(String address : Arrays.asList("fe80::9272:40ff:fe02:c363")) {
            final Socket socket = new ProxySocketFactory(new TestProtocol(), new TrustManagerHostnameCallback() {
                @Override
                public String getTarget() {
                    return "localhost";
                }
            }).withBlacklistedNetworkInterfaces(Arrays.asList("awdl0")).createSocket(address, 22);
        }
    }

    @Test
    public void testCreateSocketDualStackGoogle() throws Exception {
        final Socket socket = new ProxySocketFactory(new TestProtocol(), new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "localhost";
            }
        }).createSocket("ipv6test.google.com", 80);
        assertNotNull(socket);
        // We have set `java.net.preferIPv6Addresses` to `false` by default
        assertTrue(socket.getInetAddress() instanceof Inet4Address);
    }

    // IPv6 in the test environment
    @Test
    @Ignore
    public void testCreateSocketIPv6OnlyWithInetAddress() throws Exception {
        for(String address : Arrays.asList("ftp6.netbsd.org")) {
            final Socket socket = new ProxySocketFactory(new TestProtocol(), new TrustManagerHostnameCallback() {
                @Override
                public String getTarget() {
                    return "ftp6.netbsd.org";
                }
            }, new SocketConfigurator() {
                @Override
                public void configure(final Socket socket) throws IOException {
                    assertTrue(socket.getInetAddress() instanceof Inet6Address);
                    assertEquals(((Inet6Address) socket.getInetAddress()).getScopeId(),
                            ((Inet6Address) InetAddress.getByName("::1%en0")).getScopedInterface().getIndex());
                }
            }).withBlacklistedNetworkInterfaces(Arrays.asList("awdl0")).createSocket(address, 21);
            assertNotNull(socket);
            assertTrue(socket.getInetAddress() instanceof Inet6Address);
        }
    }

    // IPv6 in the test environment
    @Test
    @Ignore
    public void testCreateSocketIPv6OnlyUnknownDestination() throws Exception {
        for(String address : Arrays.asList("ftp6.netbsd.org")) {
            final Socket socket = new ProxySocketFactory(new TestProtocol(), new TrustManagerHostnameCallback() {
                @Override
                public String getTarget() {
                    return "ftp6.netbsd.org";
                }
            }, new SocketConfigurator() {
                @Override
                public void configure(final Socket socket) throws IOException {
                    // Not yet connected
                    assertNull(socket.getInetAddress());
                }
            }).withBlacklistedNetworkInterfaces(Arrays.asList("awdl0")).createSocket();
            assertNotNull(socket);
            assertNull(socket.getInetAddress());
            socket.connect(new InetSocketAddress(address, 21), 0);
            assertTrue(socket.getInetAddress() instanceof Inet6Address);
            assertEquals(((Inet6Address) socket.getInetAddress()).getScopeId(),
                    ((Inet6Address) InetAddress.getByName("::1%en0")).getScopedInterface().getIndex());
            assertTrue(socket.getInetAddress() instanceof Inet6Address);
        }
    }

    @Test
    public void testSpecificNetworkInterfaceForIP6Address() throws Exception {
        final InetAddress loopback = InetAddress.getByName("::1%en0");
        assertNotNull(loopback);
        assertTrue(loopback.isLoopbackAddress());
        assertTrue(loopback instanceof Inet6Address);
        assertEquals(NetworkInterface.getByName("en0").getIndex(),
                ((Inet6Address) loopback).getScopedInterface().getIndex());
    }

    // IPv6 in the test environment
    @Test
    @Ignore
    public void testDefaultNetworkInterfaceForIP6Address() throws Exception {
        assertEquals(InetAddress.getByName("::1"), InetAddress.getByName("::1%en0"));
        // Bug. Defaults to awdl0 on OS X
        assertEquals(((Inet6Address) InetAddress.getByName("::1")).getScopeId(),
                ((Inet6Address) InetAddress.getByName("::1%en0")).getScopeId());
    }

    @Test(expected = ConnectException.class)
    public void testFixDefaultNetworkInterface() throws Exception {
        final ProxySocketFactory factory = new ProxySocketFactory(new TestProtocol(), new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "localhost";
            }
        }).withBlacklistedNetworkInterfaces(Arrays.asList("awdl0"));
        assertEquals(
                ((Inet6Address) factory.createSocket("::1%en0", 80).getInetAddress()).getScopeId(),
                ((Inet6Address) factory.createSocket("::1", 80).getInetAddress()).getScopeId()
        );
    }
}