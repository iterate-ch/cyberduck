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

import org.apache.commons.net.DefaultSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;

public class HttpProxySocketFactory extends DefaultSocketFactory {

    private final Proxy proxy;

    public HttpProxySocketFactory(final Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public Socket createSocket() throws IOException {
        return new HttpProxyAwareSocket(proxy);
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return super.createSocket(host, port);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port) throws IOException {
        return super.createSocket(address, port);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        return super.createSocket(host, port, localAddr, localPort);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        return super.createSocket(address, port, localAddr, localPort);
    }
}
