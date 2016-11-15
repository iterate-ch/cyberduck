package ch.cyberduck.core.proxy;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.socket.DefaultSocketConfigurator;
import ch.cyberduck.core.socket.HttpProxySocketFactory;
import ch.cyberduck.core.socket.NetworkInterfaceAwareSocketFactory;
import ch.cyberduck.core.socket.SocketConfigurator;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;

import org.apache.commons.net.DefaultSocketFactory;
import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProxySocketFactory extends SocketFactory {
    private static final Logger log = Logger.getLogger(ProxySocketFactory.class);

    private final SocketConfigurator configurator;

    private final ProxyFinder proxyFinder;

    private final Protocol protocol;

    private final TrustManagerHostnameCallback hostnameCallback;

    private final List<Proxy.Type> types = new ArrayList<Proxy.Type>(
            Arrays.asList(Proxy.Type.DIRECT, Proxy.Type.SOCKS, Proxy.Type.HTTP, Proxy.Type.HTTPS));

    /**
     * List of ignored network interface names
     */
    private List<String> blacklisted
            = PreferencesFactory.get().getList("network.interface.blacklist");

    public ProxySocketFactory(final Protocol protocol, final TrustManagerHostnameCallback hostnameCallback) {
        this(protocol, hostnameCallback, new DefaultSocketConfigurator());
    }

    public ProxySocketFactory(final Protocol protocol, final TrustManagerHostnameCallback hostnameCallback,
                              final SocketConfigurator configurator) {
        this(protocol, hostnameCallback, configurator, ProxyFactory.get());
    }

    public ProxySocketFactory(final Protocol protocol, final TrustManagerHostnameCallback hostnameCallback,
                              final ProxyFinder proxyFinder) {
        this(protocol, hostnameCallback, new DefaultSocketConfigurator(), proxyFinder);
    }

    public ProxySocketFactory(final Protocol protocol, final TrustManagerHostnameCallback hostnameCallback,
                              final SocketConfigurator configurator,
                              final ProxyFinder proxyFinder) {
        this.protocol = protocol;
        this.hostnameCallback = hostnameCallback;
        this.configurator = configurator;
        this.proxyFinder = proxyFinder;
    }

    public ProxySocketFactory withBlacklistedNetworkInterfaces(final List<String> names) {
        this.blacklisted = names;
        return this;
    }

    /**
     * @param target Hostname
     * @return Socket factory configured with SOCKS proxy if route is determined to be proxied. Otherwise
     * direct connection socket factory.
     */
    protected SocketFactory factory(final String target) {
        final Proxy proxy = proxyFinder.find(new Host(protocol, target));
        if(!types.contains(proxy.getType())) {
            log.warn(String.format("Use of %s proxy is disabled for socket factory %s", proxy.getType(), this));
            return new NetworkInterfaceAwareSocketFactory(new DefaultSocketFactory(), blacklisted);
        }
        switch(proxy.getType()) {
            case SOCKS:
                if(log.isInfoEnabled()) {
                    log.info(String.format("Configured to use SOCKS proxy %s", proxy));
                }
                final java.net.Proxy socksProxy = new java.net.Proxy(
                        java.net.Proxy.Type.SOCKS, new InetSocketAddress(proxy.getHostname(), proxy.getPort()));
                return new NetworkInterfaceAwareSocketFactory(new DefaultSocketFactory(), blacklisted, socksProxy);
            case HTTP:
            case HTTPS:
                if(log.isInfoEnabled()) {
                    log.info(String.format("Configured to use HTTP proxy %s", proxy));
                }
                final java.net.Proxy httpProxy = new java.net.Proxy(
                        java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getHostname(), proxy.getPort()));
                return new NetworkInterfaceAwareSocketFactory(new HttpProxySocketFactory(httpProxy), blacklisted, httpProxy);
        }
        return new NetworkInterfaceAwareSocketFactory(new DefaultSocketFactory(), blacklisted);
    }

    @Override
    public Socket createSocket() throws IOException {
        final String target = hostnameCallback.getTarget();
        if(log.isInfoEnabled()) {
            log.info(String.format("Use target hostname %s determined from callback %s for proxy configuration",
                    target, hostnameCallback));
        }
        try {
            final Socket socket = this.factory(target).createSocket();
            configurator.configure(socket);
            return socket;
        }
        catch(IllegalArgumentException e) {
            throw this.failure(target, e);
        }
    }

    private IOException failure(final String target, final IllegalArgumentException e) {
        final Proxy proxy = proxyFinder.find(new Host(protocol, target));
        return new ConnectException(String.format("Unsupported proxy type %s", proxy.getType()));
    }

    @Override
    public Socket createSocket(final String hostname, final int port) throws IOException {
        try {
            final Socket socket = this.factory(hostname).createSocket(hostname, port);
            configurator.configure(socket);
            return socket;
        }
        catch(IllegalArgumentException e) {
            throw this.failure(hostname, e);
        }
    }

    @Override
    public Socket createSocket(final String hostname, final int port,
                               final InetAddress localHost, final int localPort) throws IOException {
        try {
            final Socket socket = this.factory(hostname).createSocket(hostname, port, localHost, localPort);
            configurator.configure(socket);
            return socket;
        }
        catch(IllegalArgumentException e) {
            throw this.failure(hostname, e);
        }
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int port) throws IOException {
        try {
            final Socket socket = this.factory(inetAddress.getHostName()).createSocket(inetAddress, port);
            configurator.configure(socket);
            return socket;
        }
        catch(IllegalArgumentException e) {
            throw this.failure(inetAddress.getHostName(), e);
        }
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int port,
                               final InetAddress localHost, final int localPort) throws IOException {
        try {
            final Socket socket = this.factory(inetAddress.getHostName()).createSocket(inetAddress, port, localHost, localPort);
            configurator.configure(socket);
            return socket;
        }
        catch(IllegalArgumentException e) {
            throw this.failure(inetAddress.getHostName(), e);
        }
    }

    public ProxySocketFactory disable(final Proxy.Type type) {
        types.remove(type);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProxySocketFactory{");
        sb.append("types=").append(types);
        sb.append('}');
        return sb.toString();
    }
}
