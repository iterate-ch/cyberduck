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

import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * @version $Id$
 */
public class CustomTrustSSLProtocolSocketFactory extends SSLSocketFactory {
    private static Logger log = Logger.getLogger(CustomTrustSSLProtocolSocketFactory.class);

    private SSLSocketFactory factory;

    /**
     * Shared context
     */
    private SSLContext context;

    /**
     * @param trust Verifiying trusts in system settings
     */
    public CustomTrustSSLProtocolSocketFactory(X509TrustManager trust) {
        this(trust, null);
    }

    /**
     * @param trust Verifiying trusts in system settings
     * @param key   Key manager for client certificate selection
     */
    public CustomTrustSSLProtocolSocketFactory(X509TrustManager trust, X509KeyManager key) {
        try {
            context = SSLContext.getInstance("TLS");
            context.init(new KeyManager[]{key}, new TrustManager[]{trust}, null);
            if(log.isDebugEnabled()) {
                log.debug("Using SSL context:" + context);
            }
            factory = context.getSocketFactory();
        }
        catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        catch(KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public SSLContext getSSLContext() {
        return context;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return ((SSLSocketFactory) SSLSocketFactory.getDefault()).getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return ((SSLSocketFactory) SSLSocketFactory.getDefault()).getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort)
            throws IOException {
        return factory.createSocket(host, port, clientHost, clientPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return factory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port, InetAddress localHost, int localPort) throws IOException {
        return factory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return factory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException {
        return factory.createSocket(socket, host, port, autoClose);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return context.getServerSocketFactory().createServerSocket(port);
    }
}