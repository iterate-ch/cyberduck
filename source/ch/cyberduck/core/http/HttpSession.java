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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Proxy;
import ch.cyberduck.core.ProxyFactory;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public abstract class HttpSession<C> extends SSLSession<C> {
    private static final Logger log = Logger.getLogger(HttpSession.class);

    private AbstractHttpClient client;

    /**
     * Target hostname of current request
     */
    private String target;

    protected HttpSession(final Host h) {
        super(h);

        final HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, org.apache.http.HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, getEncoding());
        HttpProtocolParams.setUserAgent(params, new PreferencesUseragentProvider().get());

        AuthParams.setCredentialCharset(params, Preferences.instance().getProperty("http.credentials.charset"));

        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setSoTimeout(params, timeout());
        HttpConnectionParams.setConnectionTimeout(params, timeout());
        HttpConnectionParams.setSocketBufferSize(params,
                Preferences.instance().getInteger("http.socket.buffer"));
        HttpConnectionParams.setStaleCheckingEnabled(params, true);

        HttpClientParams.setRedirecting(params, true);
        HttpClientParams.setAuthenticating(params, true);
        HttpClientParams.setCookiePolicy(params, CookiePolicy.BEST_MATCH);

        // Sets the timeout in milliseconds used when retrieving a connection from the ClientConnectionManager
        HttpClientParams.setConnectionManagerTimeout(params, Preferences.instance().getLong("http.manager.timeout"));

        final SSLSocketFactory sslSocketFactory = new SSLSocketFactory(
                new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()).getSSLContext(),
                new X509HostnameVerifier() {
                    @Override
                    public void verify(String host, SSLSocket ssl) throws IOException {
                        log.warn(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                    }

                    @Override
                    public void verify(String host, X509Certificate cert) throws SSLException {
                        log.warn(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                    }

                    @Override
                    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                        log.warn(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                    }

                    @Override
                    public boolean verify(String s, javax.net.ssl.SSLSession sslSession) {
                        log.warn(String.format("Hostname verification disabled for %s handled in system trust manager", host));
                        return true;
                    }
                }
        );

        final SchemeRegistry registry = new SchemeRegistry();
        // Always register HTTP for possible use with proxy. Contains a number of protocol properties such as the default port and the socket
        // factory to be used to create the java.net.Socket instances for the given protocol
        registry.register(new Scheme(ch.cyberduck.core.Scheme.http.toString(), ch.cyberduck.core.Scheme.http.getPort(),
                PlainSocketFactory.getSocketFactory()));
        registry.register(new Scheme(ch.cyberduck.core.Scheme.https.toString(), ch.cyberduck.core.Scheme.https.getPort(),
                sslSocketFactory));
        if(Preferences.instance().getBoolean("connection.proxy.enable")) {
            final Proxy proxy = ProxyFactory.get();
            if(ch.cyberduck.core.Scheme.https.equals(this.getHost().getProtocol().getScheme())) {
                if(proxy.isHTTPSProxyEnabled(host)) {
                    ConnRouteParams.setDefaultProxy(params, new HttpHost(proxy.getHTTPSProxyHost(host), proxy.getHTTPSProxyPort(host)));
                }
            }
            if(ch.cyberduck.core.Scheme.http.equals(this.getHost().getProtocol().getScheme())) {
                if(proxy.isHTTPProxyEnabled(host)) {
                    ConnRouteParams.setDefaultProxy(params, new HttpHost(proxy.getHTTPProxyHost(host), proxy.getHTTPProxyPort(host)));
                }
            }
        }
        final PoolingClientConnectionManager manager = new PoolingClientConnectionManager(registry);
        manager.setMaxTotal(Preferences.instance().getInteger("http.connections.total"));
        manager.setDefaultMaxPerRoute(Preferences.instance().getInteger("http.connections.route"));
        client = new DefaultHttpClient(manager, params);
        client.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                target = ((HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST)).getHostName();
            }
        });
        client.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                log(true, request.getRequestLine().toString());
                for(Header header : request.getAllHeaders()) {
                    log(true, header.toString());
                }
            }
        });
        client.addResponseInterceptor(new HttpResponseInterceptor() {
            @Override
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                log(false, response.getStatusLine().toString());
                for(Header header : response.getAllHeaders()) {
                    log(false, header.toString());
                }
            }
        });
        if(Preferences.instance().getBoolean("http.compression.enable")) {
            client.addRequestInterceptor(new RequestAcceptEncoding());
            client.addResponseInterceptor(new ResponseContentEncoding());
        }
    }

    @Override
    public void logout() throws BackgroundException {
        client.getConnectionManager().shutdown();
    }

    @Override
    public String getTarget() {
        return target;
    }

    public AbstractHttpClient http() {
        return client;
    }

    @Override
    public String toURL(final Path path) {
        return this.toURL(path, false);
    }
}