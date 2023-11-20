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
import ch.cyberduck.core.random.SecureRandomProviderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomTrustSSLProtocolSocketFactory extends SSLSocketFactory {
    private static final Logger log = LogManager.getLogger(CustomTrustSSLProtocolSocketFactory.class);

    private final SSLSocketFactory factory;
    /**
     * Shared context
     */
    private final SSLContext context;
    private final String[] protocols;

    private final AtomicBoolean initializer
        = new AtomicBoolean(false);

    private final Preferences preferences = PreferencesFactory.get();

    private final X509TrustManager trust;
    private final X509KeyManager key;

    /**
     * @param trust Verifying trusts in system settings
     * @param key   Key manager for client certificate selection
     */
    public CustomTrustSSLProtocolSocketFactory(final X509TrustManager trust, final X509KeyManager key) {
        this(trust, key, PreferencesFactory.get().getProperty("connection.ssl.protocols").split(","));
    }

    public CustomTrustSSLProtocolSocketFactory(final X509TrustManager trust, final X509KeyManager key, final String... protocols) {
        this(trust, key, SecureRandomProviderFactory.get().provide(), protocols);
    }

    public CustomTrustSSLProtocolSocketFactory(final X509TrustManager trust, final X509KeyManager key,
                                               final SecureRandom seeder,
                                               final String... protocols) {
        this.trust = trust;
        this.key = key;
        try {
            // Default provider
            context = SSLContext.getInstance("TLSv1.2");
            context.init(new KeyManager[]{key}, new TrustManager[]{trust}, seeder);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Using SSL context with protocol %s", context.getProtocol()));
            }
            factory = context.getSocketFactory();
        }
        catch(NoSuchAlgorithmException | KeyManagementException e) {
            throw new FactoryException(e.getMessage(), e);
        }
        this.protocols = protocols;
    }

    /**
     * @param socket    Socket to configure
     * @param protocols Enabled SSL protocol versions
     */
    protected void configure(final Socket socket, final String[] protocols) {
        if(socket instanceof SSLSocket) {
            try {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Configure SSL parameters with protocols %s", Arrays.toString(protocols)));
                }
                ((SSLSocket) socket).setEnabledProtocols(protocols);
                final List<String> ciphers = Arrays.asList(((SSLSocket) socket).getEnabledCipherSuites());
                final List<String> blacklist = preferences.getList("connection.ssl.cipher.blacklist");
                if(!blacklist.isEmpty()) {
                    ciphers.removeIf(blacklist::contains);
                }
                ((SSLSocket) socket).setEnabledCipherSuites(ciphers.toArray(new String[ciphers.size()]));
                if(log.isInfoEnabled()) {
                    log.info(String.format("Enabled cipher suites %s",
                        Arrays.toString(((SSLSocket) socket).getEnabledCipherSuites())));
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
    protected Socket handshake(final SocketGetter f) throws IOException {
        if(!initializer.get()) {
            // Load trust store before handshake
            trust.init();
            // Load key store before handshake
            key.init();
            initializer.set(true);
        }
        // Configure socket
        final Socket socket = f.create();
        this.configure(socket, protocols);
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
        return this.handshake(factory::createSocket);
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
