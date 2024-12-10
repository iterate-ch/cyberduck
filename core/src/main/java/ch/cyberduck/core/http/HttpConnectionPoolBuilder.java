package ch.cyberduck.core.http;

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

import ch.cyberduck.core.ConnectionTimeout;
import ch.cyberduck.core.DefaultConnectionTimeout;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.ProxyCredentialsStoreFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.socket.DefaultSocketConfigurator;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.AIMDBackoffManager;
import org.apache.http.impl.client.DefaultClientConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.WinHttpClients;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class HttpConnectionPoolBuilder {
    private static final Logger log = LogManager.getLogger(HttpConnectionPoolBuilder.class);

    private final ConnectionSocketFactory socketFactory;
    private final ConnectionSocketFactory sslSocketFactory;
    private final Host host;
    private final ConnectionTimeout connectionTimeout;

    public HttpConnectionPoolBuilder(final Host host,
                                     final ThreadLocalHostnameDelegatingTrustManager trust,
                                     final X509KeyManager key,
                                     final ProxyFinder proxy) {
        this(host, trust, key, new DefaultConnectionTimeout(new HostPreferences(host)), proxy);
    }

    public HttpConnectionPoolBuilder(final Host host,
                                     final ThreadLocalHostnameDelegatingTrustManager trust,
                                     final X509KeyManager key,
                                     final ConnectionTimeout connectionTimeout,
                                     final ProxyFinder proxy) {
        this(host, new PlainConnectionSocketFactory() {
            @Override
            public Socket createSocket(final HttpContext context) throws IOException {
                // Return socket factory with disabled support for HTTP tunneling as provided internally
                return new ProxySocketFactory(host, new DefaultSocketConfigurator(connectionTimeout), proxy).disable(Proxy.Type.HTTP).disable(Proxy.Type.HTTPS).createSocket();
            }

            @Override
            public Socket connectSocket(final int connectTimeout, final Socket socket, final HttpHost host,
                                        final InetSocketAddress remoteAddress, final InetSocketAddress localAddress,
                                        final HttpContext context) throws IOException {
                trust.setTarget(host.getHostName());
                return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
            }
        }, new SSLConnectionSocketFactory(
                new CustomTrustSSLProtocolSocketFactory(trust, key),
                new DisabledX509HostnameVerifier()
        ) {
            @Override
            public Socket createSocket(final HttpContext context) throws IOException {
                return new ProxySocketFactory(host, new DefaultSocketConfigurator(connectionTimeout), proxy).disable(Proxy.Type.HTTP).disable(Proxy.Type.HTTPS).createSocket();
            }

            @Override
            public Socket createLayeredSocket(final Socket socket, final String target, final int port, final HttpContext context) throws IOException {
                trust.setTarget(target);
                return super.createLayeredSocket(socket, target, port, context);
            }

            @Override
            public Socket connectSocket(final int connectTimeout, final Socket socket, final HttpHost host,
                                        final InetSocketAddress remoteAddress, final InetSocketAddress localAddress,
                                        final HttpContext context) throws IOException {
                trust.setTarget(host.getHostName());
                return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
            }
        }, connectionTimeout);
    }

    public HttpConnectionPoolBuilder(final Host host,
                                     final ConnectionSocketFactory socketFactory,
                                     final ConnectionSocketFactory sslSocketFactory,
                                     final ConnectionTimeout connectionTimeout) {
        this.host = host;
        this.socketFactory = socketFactory;
        this.sslSocketFactory = sslSocketFactory;
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * @param proxyfinder    Proxy configuration
     * @param listener Log listener
     * @param prompt   Prompt for proxy credentials
     * @return Builder for HTTP client
     */
    public HttpClientBuilder build(final ProxyFinder proxyfinder, final TranscriptListener listener, final LoginCallback prompt) {
        final HttpClientBuilder configuration = HttpClients.custom();
        configuration.setRoutePlanner(new DefaultRoutePlanner(DefaultSchemePortResolver.INSTANCE) {
            @Override
            protected HttpHost determineProxy(final HttpHost target, final HttpRequest request, final HttpContext context) {
                // Use HTTP Connect proxy implementation provided here instead of relying on internal proxy support in socket factory
                final Proxy proxy = proxyfinder.find(target.toURI());
                switch(proxy.getType()) {
                    case HTTP:
                    case HTTPS:
                        final HttpHost h = new HttpHost(proxy.getHostname(), proxy.getPort(), Scheme.http.name());
                        log.info("Setup proxy {}", h);
                        return h;
                }
                return null;
            }
        });
        configuration.setProxyAuthenticationStrategy(new CallbackProxyAuthenticationStrategy(ProxyCredentialsStoreFactory.get(), host, prompt));
        configuration.setUserAgent(new PreferencesUseragentProvider().get());
        final int timeout = connectionTimeout.getTimeout() * 1000;
        configuration.setDefaultSocketConfig(SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(timeout)
                .build());
        configuration.setDefaultRequestConfig(this.createRequestConfig(timeout));
        configuration.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setBufferSize(new HostPreferences(host).getInteger("http.socket.buffer"))
                .setCharset(Charset.forName(host.getEncoding()))
                .build());
        if(new HostPreferences(host).getBoolean("http.connections.reuse")) {
            configuration.setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy());
        }
        else {
            configuration.setConnectionReuseStrategy(new NoConnectionReuseStrategy());
        }
        if(!new HostPreferences(host).getBoolean("http.connections.state.enable")) {
            configuration.disableConnectionState();
        }
        // Retry handler for I/O failures
        configuration.setRetryHandler(new ExtendedHttpRequestRetryHandler(
                new HostPreferences(host).getInteger("connection.retry")));
        // Retry handler for HTTP error status
        configuration.setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host));
        if(!new HostPreferences(host).getBoolean("http.compression.enable")) {
            configuration.disableContentCompression();
        }
        configuration.setRequestExecutor(new CustomHttpRequestExecutor(host, listener));
        // Always register HTTP for possible use with proxy. Contains a number of protocol properties such as the
        // default port and the socket factory to be used to create the java.net.Socket instances for the given protocol
        final PoolingHttpClientConnectionManager connectionManager = this.createConnectionManager(this.createRegistry());
        configuration.setConnectionManager(connectionManager);
        configuration.setDefaultAuthSchemeRegistry(RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory(
                        Charset.forName(new HostPreferences(host).getProperty("http.credentials.charset"))))
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory(
                        Charset.forName(new HostPreferences(host).getProperty("http.credentials.charset"))))
                .register(AuthSchemes.NTLM, new HostPreferences(host).getBoolean("webdav.ntlm.windows.authentication.enable") && WinHttpClients.isWinAuthAvailable() ?
                        new BackportWindowsNTLMSchemeFactory(null) :
                        new NTLMSchemeFactory())
                .register(AuthSchemes.SPNEGO, new HostPreferences(host).getBoolean("webdav.ntlm.windows.authentication.enable") && WinHttpClients.isWinAuthAvailable() ?
                        new BackportWindowsNegotiateSchemeFactory(null) :
                        new SPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory()).build());
        if(new HostPreferences(host).getBoolean("connection.retry.backoff.enable")) {
            final AIMDBackoffManager manager = new AIMDBackoffManager(connectionManager);
            manager.setPerHostConnectionCap(new HostPreferences(host).getInteger("http.connections.route"));
            configuration.setBackoffManager(manager);
            configuration.setConnectionBackoffStrategy(new CustomConnectionBackoffStrategy(host));
        }
        return configuration;
    }

    public RequestConfig createRequestConfig(final int timeout) {
        return RequestConfig.custom()
                .setRedirectsEnabled(true)
                // Disable use of Expect: Continue by default for all methods
                .setExpectContinueEnabled(false)
                .setAuthenticationEnabled(true)
                .setConnectTimeout(timeout)
                // Sets the timeout in milliseconds used when retrieving a connection from the ClientConnectionManager
                .setConnectionRequestTimeout(new HostPreferences(host).getInteger("http.manager.timeout"))
                .setSocketTimeout(timeout)
                .setNormalizeUri(new HostPreferences(host).getBoolean("http.request.uri.normalize"))
                .build();
    }

    public Registry<ConnectionSocketFactory> createRegistry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register(Scheme.http.toString(), socketFactory)
                .register(Scheme.https.toString(), sslSocketFactory).build();
    }

    public PoolingHttpClientConnectionManager createConnectionManager(final Registry<ConnectionSocketFactory> registry) {
        log.debug("Setup connection pool with registry {}", registry);
        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry, new CustomDnsResolver());
        manager.setMaxTotal(new HostPreferences(host).getInteger("http.connections.total"));
        manager.setDefaultMaxPerRoute(new HostPreferences(host).getInteger("http.connections.route"));
        // Detect connections that have become stale (half-closed) while kept inactive in the pool
        manager.setValidateAfterInactivity(new HostPreferences(host).getInteger("http.connections.stale.check.ms"));
        return manager;
    }
}
