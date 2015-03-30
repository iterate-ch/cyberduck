package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.DisabledProxyFinder;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Proxy;
import ch.cyberduck.core.ProxyFactory;
import ch.cyberduck.core.ProxyFinder;
import ch.cyberduck.core.ProxySocketFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * @version $Id$
 */
public abstract class HttpSession<C> extends SSLSession<C> {
    private static final Logger log = Logger.getLogger(HttpSession.class);

    private Preferences preferences
            = PreferencesFactory.get();

    private HttpClientBuilder builder
            = HttpClients.custom();

    private ConnectionSocketFactory socketFactory;

    private ConnectionSocketFactory sslSocketFactory;

    protected HttpSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        this(host, trust, key, ProxyFactory.get());
    }

    protected HttpSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        this(host, trust, key, new PlainConnectionSocketFactory() {
            @Override
            public Socket createSocket(final HttpContext context) throws IOException {
                // Return socket factory with disabled support for HTTP tunneling as provided internally
                return new ProxySocketFactory(host.getProtocol(), new TrustManagerHostnameCallback() {
                    @Override
                    public String getTarget() {
                        return context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST).toString();
                    }
                }, proxy).disable(Proxy.Type.HTTP).disable(Proxy.Type.HTTPS).createSocket();
            }
        }, new SSLConnectionSocketFactory(
                new CustomTrustSSLProtocolSocketFactory(trust, key),
                new DisabledX509HostnameVerifier()
        ) {
            @Override
            public Socket createSocket(final HttpContext context) throws IOException {
                return new ProxySocketFactory(host.getProtocol(), new TrustManagerHostnameCallback() {
                    @Override
                    public String getTarget() {
                        return context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST).toString();
                    }
                }, proxy).disable(Proxy.Type.HTTP).disable(Proxy.Type.HTTPS).createSocket();
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
        }, proxy);
    }

    protected HttpSession(final Host host, final X509TrustManager trust, final X509KeyManager key,
                          final SocketFactory socketFactory) {
        this(host, trust, key, new PlainConnectionSocketFactory() {
            @Override
            public Socket createSocket(final HttpContext context) throws IOException {
                return socketFactory.createSocket();
            }
        }, new SSLConnectionSocketFactory(
                new CustomTrustSSLProtocolSocketFactory(trust, key),
                new DisabledX509HostnameVerifier()
        ) {
            @Override
            public Socket createSocket(final HttpContext context) throws IOException {
                return socketFactory.createSocket();
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
        }, new DisabledProxyFinder());
    }

    protected HttpSession(final Host host, final X509TrustManager trust, final X509KeyManager key,
                          final ConnectionSocketFactory defaultSocketFactory,
                          final ConnectionSocketFactory sslSocketFactory,
                          final ProxyFinder proxyFinder) {
        super(host, new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
        this.socketFactory = defaultSocketFactory;
        this.sslSocketFactory = sslSocketFactory;
        // Always register HTTP for possible use with proxy. Contains a number of protocol properties such as the
        // default port and the socket factory to be used to create the java.net.Socket instances for the given protocol
        final Registry<ConnectionSocketFactory> registry = this.registry().build();
        // Use HTTP Connect proxy implementation provided here instead of
        // relying on internal proxy support in socket factory
        if(preferences.getBoolean("connection.proxy.enable")) {
            final Proxy proxy = proxyFinder.find(host);
            if(proxy.getType() == Proxy.Type.HTTP) {
                final HttpHost h = new HttpHost(proxy.getHostname(), proxy.getPort(), Scheme.http.name());
                if(log.isInfoEnabled()) {
                    log.info(String.format("Setup proxy %s", h));
                }
                builder.setProxy(h);
            }
            if(proxy.getType() == Proxy.Type.HTTPS) {
                final HttpHost h = new HttpHost(proxy.getHostname(), proxy.getPort(), Scheme.https.name());
                if(log.isInfoEnabled()) {
                    log.info(String.format("Setup proxy %s", h));
                }
                builder.setProxy(h);
            }
        }
        final HttpClientConnectionManager manager = this.pool(registry);
        builder.setUserAgent(new PreferencesUseragentProvider().get());
        builder.setConnectionManager(manager);
        builder.setDefaultSocketConfig(SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(this.timeout())
                .build());
        builder.setDefaultRequestConfig(RequestConfig.custom()
                .setRedirectsEnabled(true)
                        // Disable use of Expect: Continue by default for all methods
                .setExpectContinueEnabled(false)
                .setAuthenticationEnabled(true)
                .setConnectTimeout(timeout())
                        // Sets the timeout in milliseconds used when retrieving a connection from the ClientConnectionManager
                .setConnectionRequestTimeout(preferences.getInteger("http.manager.timeout"))
                .setSocketTimeout(timeout())
                .build());
        builder.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setBufferSize(preferences.getInteger("http.socket.buffer"))
                .setCharset(Charset.forName(this.getEncoding()))
                .build());
        builder.setRetryHandler(new ExtendedHttpRequestRetryHandler(preferences.getInteger("http.connections.retry")));
        if(!preferences.getBoolean("http.compression.enable")) {
            builder.disableContentCompression();
        }
        builder.setRequestExecutor(
                new LoggingHttpRequestExecutor(this)
        );
        builder.setDefaultAuthSchemeRegistry(RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory(
                        Charset.forName(preferences.getProperty("http.credentials.charset"))))
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory(
                        Charset.forName(preferences.getProperty("http.credentials.charset"))))
                .register(AuthSchemes.NTLM, new NTLMSchemeFactory())
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())
                .build());
    }

    public HttpClientBuilder builder() {
        return builder;
    }

    protected RegistryBuilder<ConnectionSocketFactory> registry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register(Scheme.http.toString(), socketFactory)
                .register(Scheme.https.toString(), sslSocketFactory);
    }

    protected PoolingHttpClientConnectionManager pool(final Registry<ConnectionSocketFactory> registry) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Setup connection pool with registry %s", registry));
        }
        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(preferences.getInteger("http.connections.total"));
        manager.setDefaultMaxPerRoute(preferences.getInteger("http.connections.route"));
        manager.setValidateAfterInactivity(5000);
        return manager;
    }

    @Override
    public <T> T getFeature(Class<T> type) {
        if(type == Upload.class) {
            return (T) new HttpUploadFeature((AbstractHttpWriteFeature<?>) this.getFeature(Write.class));
        }
        return super.getFeature(type);
    }
}