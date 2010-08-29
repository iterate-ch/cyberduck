package ch.cyberduck.core.ssl;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * @version $Id$
 */
public class CustomTrustSSLProtocolSocketFactory extends SSLProtocolSocketFactory {
    private static Logger log = Logger.getLogger(CustomTrustSSLProtocolSocketFactory.class);

    /**
     * Trust settings
     */
    private X509TrustManager trust;

    /**
     * Shared context
     */
    private SSLContext context;

    public CustomTrustSSLProtocolSocketFactory(X509TrustManager manager) {
        this.trust = manager;
    }

    private SSLContext createCustomSSLContext(X509TrustManager trustManager) {
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, new TrustManager[]{trustManager}, null);
            return context;
        }
        catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        catch(KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public SSLContext getSSLContext() {
        if(null == context) {
            context = this.createCustomSSLContext(trust);
        }
        if(log.isDebugEnabled()) {
            log.debug("Using SSL context:" + context);
        }
        return context;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort)
            throws IOException, UnknownHostException {
        SSLSocketFactory factory = this.getSSLContext().getSocketFactory();
        return factory.createSocket(host, port, clientHost, clientPort);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params)
            throws IOException, UnknownHostException, ConnectTimeoutException {
        SSLSocketFactory factory = this.getSSLContext().getSocketFactory();
        return factory.createSocket(host, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        SSLSocketFactory factory = this.getSSLContext().getSocketFactory();
        return factory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException, UnknownHostException {
        SSLSocketFactory factory = this.getSSLContext().getSocketFactory();
        return factory.createSocket(socket, host, port, autoClose);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        SSLServerSocketFactory factory = this.getSSLContext().getServerSocketFactory();
        return factory.createServerSocket(port);
    }
}