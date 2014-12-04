package ch.cyberduck.core;

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

import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;

import org.apache.commons.net.DefaultSocketFactory;
import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @version $Id$
 */
public class ProxySocketFactory extends SocketFactory {
    private static final Logger log = Logger.getLogger(ProxySocketFactory.class);

    private SocketConfigurator configurator;

    private ProxyFinder proxyFinder;

    private Protocol protocol;

    private TrustManagerHostnameCallback hostnameCallback;

    public ProxySocketFactory(final Protocol protocol, final TrustManagerHostnameCallback hostnameCallback) {
        this(protocol, hostnameCallback, new DefaultSocketConfigurator());
    }

    public ProxySocketFactory(final Protocol protocol, final TrustManagerHostnameCallback hostnameCallback,
                              final SocketConfigurator configurator) {
        this(protocol, hostnameCallback, configurator, ProxyFactory.get());
    }

    public ProxySocketFactory(final Protocol protocol, final TrustManagerHostnameCallback hostnameCallback,
                              final SocketConfigurator configurator,
                              final ProxyFinder proxyFinder) {
        this.protocol = protocol;
        this.hostnameCallback = hostnameCallback;
        this.configurator = configurator;
        this.proxyFinder = proxyFinder;
    }

    /**
     * @param target Proxy hostname
     * @return Socket factory configured with SOCKS proxy if route is determined to be proxied. Otherwise
     * direct connection socket factory.
     */
    private SocketFactory factory(final String target) {
        if(PreferencesFactory.get().getBoolean("connection.proxy.enable")) {
            final Proxy proxy = proxyFinder.find(new Host(protocol, target));
            if(proxy.getType() == Proxy.Type.SOCKS) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Configured to use SOCKS proxy %s", proxy));
                }
                return new DefaultSocketFactory(new java.net.Proxy(
                        java.net.Proxy.Type.SOCKS, new InetSocketAddress(proxy.getHostname(), proxy.getPort())));
            }
        }
        return new DefaultSocketFactory();
    }

    @Override
    public Socket createSocket() throws IOException {
        final String target = hostnameCallback.getTarget();
        if(log.isInfoEnabled()) {
            log.info(String.format("Use target hostname %s determined from callback %s for proxy configuration",
                    hostnameCallback, target));
        }
        final Socket socket = this.factory(target).createSocket();
        configurator.configure(socket);
        return socket;
    }

    @Override
    public Socket createSocket(final String hostname, final int port) throws IOException {
        final Socket socket = this.factory(hostname).createSocket(hostname, port);
        configurator.configure(socket);
        return socket;
    }

    @Override
    public Socket createSocket(final String hostname, final int port,
                               final InetAddress localHost, final int localPort) throws IOException {
        final Socket socket = this.factory(hostname).createSocket(hostname, port, localHost, localPort);
        configurator.configure(socket);
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int port) throws IOException {
        final Socket socket = this.factory(inetAddress.getHostName()).createSocket(inetAddress, port);
        configurator.configure(socket);
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int port,
                               final InetAddress localHost, final int localPort) throws IOException {
        final Socket socket = this.factory(inetAddress.getHostName()).createSocket(inetAddress, port, localHost, localPort);
        configurator.configure(socket);
        return socket;
    }
}
