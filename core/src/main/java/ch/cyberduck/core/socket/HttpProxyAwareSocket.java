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

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.nio.charset.Charset;

/**
 * @version $Id$
 */
public class HttpProxyAwareSocket extends Socket {

    private final Proxy proxy;

    public HttpProxyAwareSocket(final Proxy proxy) {
        super(proxy.type() == Proxy.Type.HTTP ? Proxy.NO_PROXY : proxy);
        this.proxy = proxy;
    }

    public HttpProxyAwareSocket(final SocketImpl impl, final Proxy proxy) throws SocketException {
        super(impl);
        this.proxy = proxy;
    }

    public HttpProxyAwareSocket(final String host, final int port, final Proxy proxy) throws IOException {
        super(host, port);
        this.proxy = proxy;
    }

    public HttpProxyAwareSocket(final InetAddress address, final int port, final Proxy proxy) throws IOException {
        super(address, port);
        this.proxy = proxy;
    }

    public HttpProxyAwareSocket(final String host, final int port, final InetAddress localAddr, final int localPort, final Proxy proxy) throws IOException {
        super(host, port, localAddr, localPort);
        this.proxy = proxy;
    }

    public HttpProxyAwareSocket(final InetAddress address, final int port, final InetAddress localAddr, final int localPort, final Proxy proxy) throws IOException {
        super(address, port, localAddr, localPort);
        this.proxy = proxy;
    }

    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        if(proxy.type() == Proxy.Type.HTTP) {
            super.connect(proxy.address(), timeout);
            final InetSocketAddress address = (InetSocketAddress) endpoint;
            final OutputStream out = this.getOutputStream();
            IOUtils.write(String.format("CONNECT %s:%d HTTP/1.0\n\n", address.getHostName(), address.getPort()), out, Charset.defaultCharset());
            final InputStream in = this.getInputStream();
            final String response = new LineNumberReader(new InputStreamReader(in)).readLine();
            if(null == response) {
                throw new SocketException(String.format("Empty response from HTTP proxy %s", ((InetSocketAddress) proxy.address()).getHostName()));
            }
            if(response.contains("200")) {
                in.skip(in.available());
            }
            else {
                throw new SocketException(String.format("Invalid response %s from HTTP proxy %s", response, ((InetSocketAddress) proxy.address()).getHostName()));
            }
        }
        else {
            super.connect(endpoint, timeout);
        }
    }
}
