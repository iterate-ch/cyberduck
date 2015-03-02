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

import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Id$
 */
public class CustomTrustSSLProtocolSocketFactory extends SSLSocketFactory {
    private static final Logger log = Logger.getLogger(CustomTrustSSLProtocolSocketFactory.class);

    private SSLSocketFactory factory;

    /**
     * Shared context
     */
    private SSLContext context;

    private static final List<String> ENABLED_SSL_PROTOCOLS
            = new ArrayList<String>();

    private final AtomicBoolean initializer
            = new AtomicBoolean(false);

    private Preferences preferences = PreferencesFactory.get();

    private SecureRandom rpng;

    {
        final String random = PreferencesFactory.get().getProperty("connection.ssl.securerandom");
        try {
            // Obtains random numbers from the underlying native OS, without blocking to prevent
            // from excessive stalling. For example, /dev/urandom
            rpng = SecureRandom.getInstance(random);
        }
        catch(NoSuchAlgorithmException e) {
            log.error(String.format("Failure %s obtaining secure random %s", e.getMessage(), random));
        }
    }

    static {
        for(String protocol : PreferencesFactory.get().getProperty("connection.ssl.protocols").split(",")) {
            ENABLED_SSL_PROTOCOLS.add(protocol.trim());
        }
    }

    private X509TrustManager trust;

    private X509KeyManager key;

    /**
     * @param trust Verifying trusts in system settings
     * @param key   Key manager for client certificate selection
     */
    public CustomTrustSSLProtocolSocketFactory(final X509TrustManager trust, final X509KeyManager key) {
        this.trust = trust;
        this.key = key;
        try {
            context = SSLContext.getInstance("TLS");
            context.init(new KeyManager[]{key}, new TrustManager[]{trust}, rpng);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Using SSL context with protocol %s", context.getProtocol()));
            }
            factory = context.getSocketFactory();
        }
        catch(NoSuchAlgorithmException | KeyManagementException e) {
            throw new FactoryException(e.getMessage(), e);
        }
    }

    /**
     * @param socket    Socket to configure
     * @param protocols Enabled SSL protocol versions
     */
    private void configure(final Socket socket, final String[] protocols) throws IOException {
        if(socket instanceof SSLSocket) {
            try {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Configure SSL parameters with protocols %s", Arrays.toString(protocols)));
                }
                ((SSLSocket) socket).setEnabledProtocols(protocols);
                final List<String> ciphers = Arrays.asList(((SSLSocket) socket).getEnabledCipherSuites());
                final List<String> blacklist = preferences.getList("connection.ssl.cipher.blacklist");
                if(!blacklist.isEmpty()) {
                    for(Iterator<String> iter = ciphers.iterator(); iter.hasNext(); ) {
                        final String cipher = iter.next();
                        if(blacklist.contains(cipher)) {
                            iter.remove();
                        }
                    }
                }
                ((SSLSocket) socket).setEnabledCipherSuites(ciphers.toArray(new String[ciphers.size()]));
                if(log.isInfoEnabled()) {
                    log.info(String.format("Enabled cipher suites %s",
                            Arrays.toString(((SSLSocket) socket).getEnabledCipherSuites())));
                    ((SSLSocket) socket).addHandshakeCompletedListener(new HandshakeCompletedListener() {
                        @Override
                        public void handshakeCompleted(final HandshakeCompletedEvent event) {
                            log.info(String.format("Completed handshake with negotiated cipher suite %s",
                                    event.getCipherSuite()));
                            ((SSLSocket) socket).removeHandshakeCompletedListener(this);
                        }
                    });
                }
            }
            catch(Exception e) {
                log.warn(String.format("Failed to configure SSL parameters %s", e.getMessage()));
            }
        }
    }

    /**
     * @param f Socket factory
     * @return Configured socket
     * @throws IOException Error creating socket
     */
    private Socket handshake(final SocketGetter f) throws IOException {
        if(!initializer.get()) {
            // Load trust store before handshake
            trust.init();
            // Load key store before handshake
            key.init();
            initializer.set(true);
        }
        // Configure socket
        final Socket socket = f.create();
        this.configure(socket, ENABLED_SSL_PROTOCOLS.toArray(new String[ENABLED_SSL_PROTOCOLS.size()]));
        if(log.isDebugEnabled()) {
            log.debug(String.format("Handshake for socket %s", socket));
        }
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
    public Socket createSocket() throws IOException {
        return this.handshake(new SocketGetter() {
            @Override
            public Socket create() throws IOException {
                return factory.createSocket();
            }
        });
    }

    @Override
    public Socket createSocket(final String host, final int port,
                               final InetAddress clientHost, final int clientPort)
            throws IOException {
        return this.handshake(new SocketGetter() {
            @Override
            public Socket create() throws IOException {
                return factory.createSocket(host, port, clientHost, clientPort);
            }
        });
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        return this.handshake(new SocketGetter() {
            @Override
            public Socket create() throws IOException {
                return factory.createSocket(host, port);
            }
        });
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port,
                               final InetAddress localHost, final int localPort) throws IOException {
        return this.handshake(new SocketGetter() {
            @Override
            public Socket create() throws IOException {
                return factory.createSocket(host, port, localHost, localPort);
            }
        });
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return this.handshake(new SocketGetter() {
            @Override
            public Socket create() throws IOException {
                return factory.createSocket(host, port);
            }
        });
    }

    @Override
    public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
            throws IOException {
        return this.handshake(new SocketGetter() {
            @Override
            public Socket create() throws IOException {
                return factory.createSocket(socket, host, port, autoClose);
            }
        });
    }
}