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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Proxy;
import ch.cyberduck.core.ProxyFactory;
import ch.cyberduck.core.ProxySocketFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * @version $Id$
 */
public abstract class HttpSession<C> extends SSLSession<C> {
    private static final Logger log = Logger.getLogger(HttpSession.class);

    private DisabledX509HostnameVerifier hostnameVerifier
            = new DisabledX509HostnameVerifier();

    private Preferences preferences
            = PreferencesFactory.get();

    private HttpClientBuilder builder;

    protected HttpSession(final Host host) {
        super(host);
        hostnameVerifier.setTarget(host.getHostname());
    }

    protected HttpSession(final Host host, final X509TrustManager manager) {
        super(host, manager);
        hostnameVerifier.setTarget(host.getHostname());
    }

    protected HttpSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
        hostnameVerifier.setTarget(host.getHostname());
    }

    public HttpClientBuilder builder(final TranscriptListener transcript) {
        if(null == builder) {
            builder = HttpClients.custom();
            // Always register HTTP for possible use with proxy. Contains a number of protocol properties such as the
            // default port and the socket factory to be used to create the java.net.Socket instances for the given protocol
            final Registry<ConnectionSocketFactory> registry = this.registry().build();
            if(preferences.getBoolean("connection.proxy.enable")) {
                final Proxy proxy = ProxyFactory.get().find(host);
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
                    .setStaleConnectionCheckEnabled(true)
                    .setSocketTimeout(timeout())
                    .build());
            builder.setDefaultConnectionConfig(ConnectionConfig.custom()
                    .setBufferSize(preferences.getInteger("http.socket.buffer"))
                    .setCharset(Charset.forName(this.getEncoding()))
                    .build());
            builder.setRetryHandler(new DisabledHttpRequestRetryHandler());
            builder.addInterceptorLast(new HttpRequestInterceptor() {
                @Override
                public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                    hostnameVerifier.setTarget(((HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST)).getHostName());
                }
            });
            if(!preferences.getBoolean("http.compression.enable")) {
                builder.disableContentCompression();
            }
            builder.setRequestExecutor(
                    new LoggingHttpRequestExecutor(transcript)
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
        return builder;
    }

    protected RegistryBuilder<ConnectionSocketFactory> registry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register(Scheme.http.toString(), new PlainConnectionSocketFactory() {
                    @Override
                    public Socket createSocket(final HttpContext context) throws IOException {
                        return new ProxySocketFactory(host.getProtocol(), new TrustManagerHostnameCallback() {
                            @Override
                            public String getTarget() {
                                return context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST).toString();
                            }
                        }).createSocket();
                    }
                })
                .register(Scheme.https.toString(), new SSLConnectionSocketFactory(
                        new CustomTrustSSLProtocolSocketFactory(this.getTrustManager(), this.getKeyManager()),
                        hostnameVerifier
                ) {
                    @Override
                    public Socket createSocket(final HttpContext context) throws IOException {
                        return new ProxySocketFactory(host.getProtocol(), new TrustManagerHostnameCallback() {
                            @Override
                            public String getTarget() {
                                return context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST).toString();
                            }
                        }).createSocket();
                    }

                    @Override
                    public Socket connectSocket(final int connectTimeout,
                                                final Socket socket,
                                                final HttpHost host,
                                                final InetSocketAddress remoteAddress,
                                                final InetSocketAddress localAddress,
                                                final HttpContext context) throws IOException {
                        hostnameVerifier.setTarget(remoteAddress.getHostName());
                        return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
                    }
                });
    }

    protected PoolingHttpClientConnectionManager pool(final Registry<ConnectionSocketFactory> registry) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Setup connection pool with registry %s", registry));
        }
        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(preferences.getInteger("http.connections.total"));
        manager.setDefaultMaxPerRoute(preferences.getInteger("http.connections.route"));
        return manager;
    }

    @Override
    public String getTarget() {
        return hostnameVerifier.getTarget();
    }

    @Override
    public <T> T getFeature(Class<T> type) {
        if(type == Upload.class) {
            return (T) new HttpUploadFeature((AbstractHttpWriteFeature<?>) this.getFeature(Write.class));
        }
        return super.getFeature(type);
    }

}