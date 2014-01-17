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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Proxy;
import ch.cyberduck.core.ProxyFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
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
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.Security;
import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public abstract class HttpSession<C> extends SSLSession<C> {
    private static final Logger log = Logger.getLogger(HttpSession.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Target hostname of current request stored as thread local
     */
    private ThreadLocal<String> target
            = new ThreadLocal<String>();

    protected HttpSession(final Host host) {
        super(host);
        target.set(host.getHostname());
    }

    protected HttpSession(final Host host, final X509TrustManager manager) {
        super(host, manager);
        target.set(host.getHostname());
    }

    public HttpClientBuilder connect() {
        // Always register HTTP for possible use with proxy. Contains a number of protocol properties such as the default port and the socket
        // factory to be used to create the java.net.Socket instances for the given protocol
        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(Scheme.http.toString(), PlainConnectionSocketFactory.getSocketFactory())
                .register(Scheme.https.toString(), new SSLConnectionSocketFactory(
                        new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()),
                        new X509HostnameVerifier() {
                            @Override
                            public void verify(final String host, final SSLSocket socket) throws IOException {
                                log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                                target.set(host);
                            }

                            @Override
                            public void verify(final String host, final X509Certificate cert) throws SSLException {
                                log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                                target.set(host);
                            }

                            @Override
                            public void verify(final String host, final String[] cns, final String[] subjectAlts) throws SSLException {
                                log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                                target.set(host);
                            }

                            @Override
                            public boolean verify(String s, final javax.net.ssl.SSLSession sslSession) {
                                log.debug(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                                return true;
                            }
                        }
                ) {
                    @Override
                    public Socket connectSocket(final int connectTimeout,
                                                final Socket socket,
                                                final HttpHost host,
                                                final InetSocketAddress remoteAddress,
                                                final InetSocketAddress localAddress,
                                                final HttpContext context) throws IOException {
                        target.set(remoteAddress.getHostName());
                        return super.connectSocket(connectTimeout, socket, host, remoteAddress, localAddress, context);
                    }
                }).build();
        final HttpClientBuilder builder = HttpClients.custom();
        if(Preferences.instance().getBoolean("connection.proxy.enable")) {
            final Proxy proxy = ProxyFactory.get();
            if(ch.cyberduck.core.Scheme.https.equals(this.getHost().getProtocol().getScheme())) {
                if(proxy.isHTTPSProxyEnabled(host)) {
                    builder.setProxy(new HttpHost(proxy.getHTTPSProxyHost(host), proxy.getHTTPSProxyPort(host)));
                }
            }
            if(ch.cyberduck.core.Scheme.http.equals(this.getHost().getProtocol().getScheme())) {
                if(proxy.isHTTPProxyEnabled(host)) {
                    builder.setProxy(new HttpHost(proxy.getHTTPProxyHost(host), proxy.getHTTPProxyPort(host)));
                }
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
                .setExpectContinueEnabled(true)
                .setAuthenticationEnabled(true)
                .setConnectTimeout(timeout())
                        // Sets the timeout in milliseconds used when retrieving a connection from the ClientConnectionManager
                .setConnectionRequestTimeout(Preferences.instance().getInteger("http.manager.timeout"))
                .setStaleConnectionCheckEnabled(true)
                .setSocketTimeout(timeout())
                .build());
        builder.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setBufferSize(Preferences.instance().getInteger("http.socket.buffer"))
                .setCharset(Charset.forName(getEncoding()))
                .build());
        builder.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                target.set(((HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST)).getHostName());
            }
        });
        builder.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                log(true, request.getRequestLine().toString());
                for(Header header : request.getAllHeaders()) {
                    log(true, header.toString());
                }
            }
        });
        builder.addInterceptorLast(new HttpResponseInterceptor() {
            @Override
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                log(false, response.getStatusLine().toString());
                for(Header header : response.getAllHeaders()) {
                    log(false, header.toString());
                }
            }
        });
        if(Preferences.instance().getBoolean("http.compression.enable")) {
            builder.addInterceptorLast(new RequestAcceptEncoding());
            builder.addInterceptorLast(new ResponseContentEncoding());
        }
        builder.setDefaultAuthSchemeRegistry(RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory(
                        Charset.forName(Preferences.instance().getProperty("http.credentials.charset"))))
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory(
                        Charset.forName(Preferences.instance().getProperty("http.credentials.charset"))))
                .register(AuthSchemes.NTLM, new NTLMSchemeFactory())
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())
                .build());
        return builder;
    }

    protected PoolingHttpClientConnectionManager pool(final Registry<ConnectionSocketFactory> registry) {
        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(Preferences.instance().getInteger("http.connections.total"));
        manager.setDefaultMaxPerRoute(Preferences.instance().getInteger("http.connections.route"));
        return manager;
    }

    @Override
    public String getTarget() {
        return target.get();
    }

    @Override
    public <T> T getFeature(Class<T> type) {
        if(type == Upload.class) {
            return (T) new HttpUploadFeature((AbstractHttpWriteFeature<?>) this.getFeature(Write.class));
        }
        return super.getFeature(type);
    }
}