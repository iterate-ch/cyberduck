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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.net.DefaultSocketFactory;
import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import sun.net.util.IPAddressUtil;

/**
 * Override default network interface for IPv6 to en0 instead of awdl0 set in <code>java.net.DefaultInterface#getDefault()</code>.
 */
public class NetworkInterfaceAwareSocketFactory extends SocketFactory {
    private static final Logger log = Logger.getLogger(NetworkInterfaceAwareSocketFactory.class);

    private final Proxy proxy;

    private final List<String> blacklisted;

    private final SocketFactory delegate;

    public NetworkInterfaceAwareSocketFactory() {
        this(new DefaultSocketFactory());
    }

    public NetworkInterfaceAwareSocketFactory(final SocketFactory delegate) {
        this(delegate, PreferencesFactory.get().getList("network.interface.blacklist"), null);
    }

    public NetworkInterfaceAwareSocketFactory(final java.net.Proxy proxy) {
        this(new DefaultSocketFactory(), proxy);
    }

    public NetworkInterfaceAwareSocketFactory(final SocketFactory delegate, final java.net.Proxy proxy) {
        this(delegate, PreferencesFactory.get().getList("network.interface.blacklist"), proxy);
    }

    /**
     * @param blacklisted Network interface names to ignore
     */
    public NetworkInterfaceAwareSocketFactory(final List<String> blacklisted) {
        this(new DefaultSocketFactory(), blacklisted);
    }

    public NetworkInterfaceAwareSocketFactory(final SocketFactory delegate, final List<String> blacklisted) {
        this(delegate, blacklisted, null);
    }

    /**
     * @param blacklisted Network interface names to ignore
     * @param proxy       Proxy or null for direct connection
     */
    public NetworkInterfaceAwareSocketFactory(final SocketFactory delegate, final List<String> blacklisted, final java.net.Proxy proxy) {
        this.delegate = delegate;
        this.blacklisted = blacklisted;
        this.proxy = null == proxy ? Proxy.NO_PROXY : proxy;
    }

    @Override
    public Socket createSocket() throws IOException {
        return new HttpProxyAwareSocket(proxy) {
            @Override
            public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
                if(endpoint instanceof InetSocketAddress) {
                    final InetSocketAddress address = (InetSocketAddress) endpoint;
                    if(address.getAddress() instanceof Inet6Address) {
                        final NetworkInterface network = findIPv6Interface((Inet6Address) address.getAddress());
                        if(null != network) {
                            super.connect(new InetSocketAddress(
                                    NetworkInterfaceAwareSocketFactory.this.getByAddressForInterface(network, address.getAddress()),
                                    address.getPort()), timeout);
                            return;
                        }
                    }
                }
                super.connect(endpoint, timeout);
            }
        };
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        return this.createSocket(InetAddress.getByName(host), port, localAddr, localPort);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        if(address instanceof Inet6Address) {
            final NetworkInterface network = this.findIPv6Interface((Inet6Address) address);
            if(null == network) {
                return delegate.createSocket(address, port, localAddr, localPort);
            }
            return delegate.createSocket(this.getByAddressForInterface(network, address), port, localAddr, localPort);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Use default network interface to bind %s", address));
        }
        return delegate.createSocket(address, port, localAddr, localPort);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return this.createSocket(InetAddress.getByName(host), port);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port) throws IOException {
        if(address instanceof Inet6Address) {
            final NetworkInterface network = this.findIPv6Interface((Inet6Address) address);
            if(null == network) {
                return delegate.createSocket(address, port);
            }
            return delegate.createSocket(this.getByAddressForInterface(network, address), port);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Use default network interface to bind %s", address));
        }
        return delegate.createSocket(address, port);
    }

    /**
     * @param network Network interface index
     * @throws UnknownHostException
     */
    private Inet6Address getByAddressForInterface(final NetworkInterface network, final InetAddress address) throws UnknownHostException {
        // Append network interface. Workaround for issue #8802
        return Inet6Address.getByAddress(address.getHostAddress(),
                IPAddressUtil.textToNumericFormatV6(address.getHostAddress()), network.getIndex());
    }

    private NetworkInterface findIPv6Interface(Inet6Address address) throws IOException {
        if(blacklisted.isEmpty()) {
            if(log.isDebugEnabled()) {
                log.debug("Ignore IP6 default network interface setup with empty blacklist");
            }
            return null;
        }
        if(address.getScopeId() != 0) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Ignore IP6 default network interface setup for address with scope identifier %d", address.getScopeId()));
            }
            return null;
        }
        // If we find an interface name en0 that supports IPv6 make it the default.
        // We must use the index of the network interface. Referencing the interface by name will still
        // set the scope id to '0' referencing the awdl0 interface that is first in the list of enumerated
        // network interfaces instead of its correct index in <code>java.net.Inet6Address</code>
        // Use private API to defer the numeric format for the address
        List<Integer> indexes = new ArrayList<Integer>();
        final Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
        while(enumeration.hasMoreElements()) {
            indexes.add(enumeration.nextElement().getIndex());
        }
        for(Integer index : indexes) {
            final NetworkInterface n = NetworkInterface.getByIndex(index);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Evaluate interface with %s index %d", n, index));
            }
            if(!n.isUp()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Ignore interface %s not up", n));
                }
                continue;
            }
            if(blacklisted.contains(n.getName())) {
                log.warn(String.format("Ignore network interface %s disabled with blacklist", n));
                continue;
            }
            for(InterfaceAddress i : n.getInterfaceAddresses()) {
                if(i.getAddress() instanceof Inet6Address) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Selected network interface %s", n));
                    }
                    return n;
                }
            }
            log.warn(String.format("No IPv6 for interface %s", n));
        }
        log.warn("No network interface found for IPv6");
        return null;
    }
}
