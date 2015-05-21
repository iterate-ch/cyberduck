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

import ch.cyberduck.core.proxy.ProxySocketFactory;

import org.apache.commons.net.DefaultSocketFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;

import sun.net.util.IPAddressUtil;

/**
 * Override default network interface for IPv6 to en0 instead of awdl0 set in <code>java.net.DefaultInterface#getDefault()</code>.
 */
public class NetworkInterfaceAwareSocketFactory extends DefaultSocketFactory {
    private static final Logger log = Logger.getLogger(ProxySocketFactory.class);

    public NetworkInterfaceAwareSocketFactory() {
        super();
    }

    public NetworkInterfaceAwareSocketFactory(final java.net.Proxy proxy) {
        super(proxy);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return this.createSocket(InetAddress.getByName(host), port);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port) throws IOException {
        if(address instanceof Inet6Address) {
            // If we find an interface name en0 that supports IPv6 make it the default.
            // We must use the index of the network interface. Referencing the interface by name will still
            // set the scope id to '0' referencing the awdl0 interface that is first in the list of enumerated
            // network interfaces instead of its correct index in <code>java.net.Inet6Address</code>
            // Use private API to defer the numeric format for the address
            final NetworkInterface en0 = NetworkInterface.getByName("en0");
            if(null == en0) {
                // Interface is not found when link is down #fail
                log.warn("No network interface named en0");
                return super.createSocket(address, port);
            }
            if(!en0.isUp()) {
                log.warn(String.format("Network interface %s is down", en0));
                return super.createSocket(address, port);
            }
            for(InterfaceAddress i : en0.getInterfaceAddresses()) {
                if(i.getAddress() instanceof Inet6Address) {
                    // Append network interface. Workaround for issue #8802
                    return super.createSocket(Inet6Address.getByAddress(address.getHostAddress(),
                            IPAddressUtil.textToNumericFormatV6(address.getHostAddress()), en0.getIndex()), port);
                }
            }
            log.warn(String.format("No IPv6 for interface %s", en0));
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Use default network interface to bind %s", address));
        }
        return super.createSocket(address, port);
    }
}
