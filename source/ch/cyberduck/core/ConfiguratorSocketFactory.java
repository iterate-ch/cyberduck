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

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @version $Id$
 */
public class ConfiguratorSocketFactory extends SocketFactory {

    private final SocketFactory delegate
            = SocketFactory.getDefault();

    private SocketConfigurator configurator;

    public ConfiguratorSocketFactory() {
        this(new DefaultSocketConfigurator());
    }

    public ConfiguratorSocketFactory(SocketConfigurator configurator) {
        this.configurator = configurator;
    }

    @Override
    public Socket createSocket() throws IOException {
        final Socket socket = delegate.createSocket();
        configurator.configure(socket);
        return socket;
    }

    @Override
    public Socket createSocket(String hostname, int port) throws IOException {
        final Socket socket = delegate.createSocket(hostname, port);
        configurator.configure(socket);
        return socket;
    }

    @Override
    public Socket createSocket(String hostname, int port, InetAddress localHost, int localPort) throws IOException {
        final Socket socket = delegate.createSocket(hostname, port, localHost, localPort);
        configurator.configure(socket);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int port) throws IOException {
        final Socket socket = delegate.createSocket(inetAddress, port);
        configurator.configure(socket);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int port, InetAddress localHost, int localPort) throws IOException {
        final Socket socket = delegate.createSocket(inetAddress, port, localHost, localPort);
        configurator.configure(socket);
        return socket;
    }
}
