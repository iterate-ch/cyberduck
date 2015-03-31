package ch.cyberduck.core.udt;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.DefaultSocketConfigurator;
import ch.cyberduck.core.Header;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.SocketConfigurator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.http.DisabledX509HostnameVerifier;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.TemporaryApplicationResourcesFinder;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import com.barchart.udt.ResourceUDT;

/**
 * @version $Id$
 */
public class UDTProxyConfigurator implements TrustManagerHostnameCallback {
    private static final Logger log = Logger.getLogger(UDTProxyConfigurator.class);

    private Preferences preferences
            = PreferencesFactory.get();

    private Location.Name location;

    /**
     * Transparent HTTP over UDT Proxy
     */
    private UDTProxyProvider provider;

    private X509TrustManager trust;

    private X509KeyManager key;

    private UDTCallback callback;

    private SocketConfigurator configurator
            = new DefaultSocketConfigurator();

    public UDTProxyConfigurator(final Location.Name location, final UDTProxyProvider provider) {
        this.location = location;
        this.provider = provider;
        this.trust = new KeychainX509TrustManager(this);
        this.key = new KeychainX509KeyManager();
        this.callback = new DisabledUDTCallback();
    }

    public UDTProxyConfigurator(final Location.Name location, final UDTProxyProvider provider,
                                final X509TrustManager trust) {
        this.location = location;
        this.provider = provider;
        this.trust = trust;
        this.key = new KeychainX509KeyManager();
        this.callback = new DisabledUDTCallback();
    }

    public UDTProxyConfigurator(final Location.Name location, final UDTProxyProvider provider,
                                final X509TrustManager trust, final X509KeyManager key) {
        this.location = location;
        this.provider = provider;
        this.trust = trust;
        this.key = key;
        this.callback = new DisabledUDTCallback();
    }

    public UDTProxyConfigurator(final Location.Name location, final UDTProxyProvider provider,
                                final X509TrustManager trust, final X509KeyManager key,
                                final UDTCallback callback) {
        this.location = location;
        this.provider = provider;
        this.trust = trust;
        this.key = key;
        this.callback = callback;
    }

    static {
        ResourceUDT.setLibraryExtractLocation(new TemporaryApplicationResourcesFinder().find().getAbsolute());
    }

    @Override
    public String getTarget() {
        return provider.find(location, true).getHostname();
    }

    /**
     * Configure the HTTP Session to proxy through UDT
     */
    public void configure(final HttpSession session) throws BackgroundException {
        // Add X-Qloudsonic-* headers
        final List<Header> headers = provider.headers();
        if(log.isInfoEnabled()) {
            log.info(String.format("Obtained headers %s fro provider %s", headers, provider));
        }
        // Run through secured proxy only if direct connection has transport security
        final Host proxy = provider.find(location, session.getHost().getProtocol().isSecure());
        final HttpClientBuilder builder = session.builder();
        builder.setRequestExecutor(
                new HttpRequestExecutor() {
                    @Override
                    public HttpResponse execute(final HttpRequest request, final HttpClientConnection conn, final HttpContext context) throws IOException, HttpException {
                        for(Header h : headers) {
                            request.addHeader(new BasicHeader(h.getName(), h.getValue()));
                        }
                        return super.execute(request, conn, context);
                    }
                }
        );
        builder.setRoutePlanner(new DefaultProxyRoutePlanner(
                new HttpHost(proxy.getHostname(), proxy.getPort(), proxy.getProtocol().getScheme().name()),
                new DefaultSchemePortResolver()));
        final RegistryBuilder<ConnectionSocketFactory> registry = RegistryBuilder.create();
        if(proxy.getProtocol().isSecure()) {
            registry.register(proxy.getProtocol().getScheme().toString(), new SSLConnectionSocketFactory(
                    new CustomTrustSSLProtocolSocketFactory(trust, key),
                    new DisabledX509HostnameVerifier() {
                        @Override
                        public boolean verify(final String host, final javax.net.ssl.SSLSession sslSession) {
                            if(trust instanceof ThreadLocalHostnameDelegatingTrustManager) {
                                ((ThreadLocalHostnameDelegatingTrustManager) trust).setTarget(host);
                            }
                            return true;
                        }
                    }

            ) {
                @Override
                public Socket createSocket(final HttpContext context) throws IOException {
                    final UDTSocket socket = new UDTSocket();
                    configurator.configure(socket);
                    callback.socketCreated(socket);
                    return socket;
                }

                @Override
                public Socket connectSocket(final int connectTimeout,
                                            final Socket socket,
                                            final HttpHost host,
                                            final InetSocketAddress remoteAddress,
                                            final InetSocketAddress localAddress,
                                            final HttpContext context) throws IOException {
                    if(trust instanceof ThreadLocalHostnameDelegatingTrustManager) {
                        ((ThreadLocalHostnameDelegatingTrustManager) trust).setTarget(remoteAddress.getHostName());
                    }
                    return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
                }
            });
        }
        else {
            registry.register(proxy.getProtocol().getScheme().toString(), new PlainConnectionSocketFactory() {
                @Override
                public Socket createSocket(final HttpContext context) throws IOException {
                    final UDTSocket socket = new UDTSocket();
                    configurator.configure(socket);
                    callback.socketCreated(socket);
                    return socket;
                }
            });
        }
        registry.register(Scheme.https.toString(), new SSLConnectionSocketFactory(
                new CustomTrustSSLProtocolSocketFactory(trust, key),
                new DisabledX509HostnameVerifier()

        ) {
            @Override
            public Socket createSocket(HttpContext context) throws IOException {
                final Socket socket = super.createSocket(context);
                configurator.configure(socket);
                return socket;
            }

            @Override
            public Socket connectSocket(final int connectTimeout,
                                        final Socket socket,
                                        final HttpHost host,
                                        final InetSocketAddress remoteAddress,
                                        final InetSocketAddress localAddress,
                                        final HttpContext context) throws IOException {
                if(trust instanceof ThreadLocalHostnameDelegatingTrustManager) {
                    ((ThreadLocalHostnameDelegatingTrustManager) trust).setTarget(remoteAddress.getHostName());
                }
                return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
            }
        });
        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry.build());
        manager.setMaxTotal(preferences.getInteger("http.connections.total"));
        manager.setDefaultMaxPerRoute(preferences.getInteger("http.connections.route"));
        builder.setConnectionManager(manager);
    }

}
