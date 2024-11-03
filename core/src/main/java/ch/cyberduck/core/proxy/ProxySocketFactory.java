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
import ch.cyberduck.core.socket.DefaultSocketConfigurator;
import ch.cyberduck.core.socket.HttpProxySocketFactory;
import ch.cyberduck.core.socket.SocketConfigurator;

import org.apache.commons.net.DefaultSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger log = LogManager.getLogger(ProxySocketFactory.class);

    private final SocketConfigurator configurator;
    private final ProxyFinder proxyFinder;
    private final Host host;

    private final List<Proxy.Type> types = new ArrayList<>(
            Arrays.asList(Proxy.Type.DIRECT, Proxy.Type.SOCKS, Proxy.Type.HTTP, Proxy.Type.HTTPS));

    public ProxySocketFactory(final Host host) {
        this(host, new DefaultSocketConfigurator());
    }

    public ProxySocketFactory(final Host host,
                              final SocketConfigurator configurator) {
        this(host, configurator, ProxyFactory.get());
    }

    public ProxySocketFactory(final Host host,
                              final ProxyFinder proxyFinder) {
        this(host, new DefaultSocketConfigurator(), proxyFinder);
    }

    public ProxySocketFactory(final Host host,
                              final SocketConfigurator configurator,
                              final ProxyFinder proxyFinder) {
        this.host = host;
        this.configurator = configurator;
        this.proxyFinder = proxyFinder;
    }

    /**
     * @return Socket factory configured with SOCKS proxy if route is determined to be proxied. Otherwise
     * direct connection socket factory.
     */
    protected SocketFactory factory() {
        final Proxy proxy = proxyFinder.find(new ProxyHostUrlProvider().get(host));
        if(!types.contains(proxy.getType())) {
            log.warn("Use of {} proxy is disabled for socket factory {}", proxy.getType(), this);
            return new DefaultSocketFactory();
        }
        switch(proxy.getType()) {
            case SOCKS:
                log.info("Configured to use SOCKS proxy {}", proxy);
                final java.net.Proxy socksProxy = new java.net.Proxy(
                    java.net.Proxy.Type.SOCKS, new InetSocketAddress(proxy.getHostname(), proxy.getPort()));
                return new DefaultSocketFactory(socksProxy);
            case HTTP:
            case HTTPS:
                log.info("Configured to use HTTP proxy {}", proxy);
                final java.net.Proxy httpProxy = new java.net.Proxy(
                    java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getHostname(), proxy.getPort()));
                return new HttpProxySocketFactory(httpProxy);
        }
        return new DefaultSocketFactory();
    }

    @Override
    public Socket createSocket() throws IOException {
        try {
            final Socket socket = this.factory().createSocket();
            configurator.configure(socket);
            return socket;
        }
        catch(IllegalArgumentException e) {
            throw this.failure(host.getHostname(), e);
        }
    }

    private IOException failure(final String target, final IllegalArgumentException e) {
        return new ConnectException(String.format("Unsupported proxy type for target %s", target));
    }

    @Override
    public Socket createSocket(final String hostname, final int port) throws IOException {
        try {
            final Socket socket = this.factory().createSocket(hostname, port);
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
            final Socket socket = this.factory().createSocket(hostname, port, localHost, localPort);
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
            final Socket socket = this.factory().createSocket(inetAddress, port);
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
            final Socket socket = this.factory().createSocket(inetAddress, port, localHost, localPort);
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
