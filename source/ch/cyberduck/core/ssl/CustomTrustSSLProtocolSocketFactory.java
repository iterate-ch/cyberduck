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

import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private static final List<String> ENABLED_SSL_PROTOCOLS = new ArrayList<String>();

    static {
        for(String protocol : Preferences.instance().getProperty("connection.ssl.protocols").split(",")) {
            ENABLED_SSL_PROTOCOLS.add(protocol.trim());
        }
    }

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

    /**
     * @param socket
     * @param protocols
     * @return
     * @throws IOException
     */
    private void configure(Socket socket, String[] protocols) throws IOException {
        if(socket instanceof SSLSocket) {
            SSLParameters parameters = ((SSLSocket) socket).getSSLParameters();
            log.debug("Configure SSL parameters with protocol:" + Arrays.toString(protocols));
            parameters.setProtocols(protocols);
            ((SSLSocket) socket).setSSLParameters(parameters);
        }
    }

    /**
     *
     * @param f
     * @return
     * @throws IOException
     */
    private Socket handshake(SocketGetter f) throws IOException {
        Socket socket = f.create();
        try {
            this.configure(socket, ENABLED_SSL_PROTOCOLS.<String>toArray(new String[ENABLED_SSL_PROTOCOLS.size()]));
            log.debug("handhsake:" + socket);
            //((SSLSocket) socket).startHandshake();
        }
        catch(IOException e) {
            log.warn("Handshake failed for:" + e.getMessage());
            throw e;
        }
        // Handshake succeeded.
        return socket;
    }

    private interface SocketGetter {
        Socket create() throws IOException;
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
    public Socket createSocket(final String host, final int port, final InetAddress clientHost, final int clientPort)
            throws IOException {
        return this.handshake(new SocketGetter() {
            public Socket create() throws IOException {
                return factory.createSocket(host, port, clientHost, clientPort);
            }
        });
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        return this.handshake(new SocketGetter() {
            public Socket create() throws IOException {
                return factory.createSocket(host, port);
            }
        });
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port, final InetAddress localHost, final int localPort) throws IOException {
        return this.handshake(new SocketGetter() {
            public Socket create() throws IOException {
                return factory.createSocket(host, port, localHost, localPort);
            }
        });
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return this.handshake(new SocketGetter() {
            public Socket create() throws IOException {
                return factory.createSocket(host, port);
            }
        });
    }

    @Override
    public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
            throws IOException {
        return this.handshake(new SocketGetter() {
            public Socket create() throws IOException {
                return factory.createSocket(socket, host, port, autoClose);
            }
        });
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return context.getServerSocketFactory().createServerSocket(port);
    }
}