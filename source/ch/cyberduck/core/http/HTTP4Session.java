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
import ch.cyberduck.core.Proxy;
import ch.cyberduck.core.ProxyFactory;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.http.*;
import org.apache.http.auth.params.AuthParams;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * @version $Id: HTTPSession.java 7171 2010-10-02 15:06:28Z dkocher $
 */
public abstract class HTTP4Session extends SSLSession {

    protected HTTP4Session(Host h) {
        super(h);
    }

    private AbstractHttpClient http;

    /**
     * Create new HTTP client with default configuration and custom trust manager.
     *
     * @return A new instance of a default HTTP client.
     */
    protected AbstractHttpClient http() {
        if(null == http) {
            final HttpParams params = new BasicHttpParams();

            HttpProtocolParams.setVersion(params, org.apache.http.HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, getEncoding());
            HttpProtocolParams.setUserAgent(params, getUserAgent());

            AuthParams.setCredentialCharset(params, "ISO-8859-1");

            HttpConnectionParams.setTcpNoDelay(params, true);
            HttpConnectionParams.setSoTimeout(params, timeout());
            HttpConnectionParams.setSocketBufferSize(params, 8192);

            HttpClientParams.setRedirecting(params, true);
            HttpClientParams.setAuthenticating(params, true);

            SchemeRegistry registry = new SchemeRegistry();
            // Always register HTTP for possible use with proxy
            registry.register(new Scheme("http", host.getPort(), PlainSocketFactory.getSocketFactory()));
            if("https".equals(this.getHost().getProtocol().getScheme())) {
                org.apache.http.conn.ssl.SSLSocketFactory factory = new SSLSocketFactory(
                        new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()).getSSLContext(),
                        org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                registry.register(new Scheme(host.getProtocol().getScheme(), host.getPort(), factory));
            }
            if(Preferences.instance().getBoolean("connection.proxy.enable")) {
                final Proxy proxy = ProxyFactory.instance();
                if("https".equals(this.getHost().getProtocol().getScheme())) {
                    if(proxy.isHTTPSProxyEnabled(host)) {
                        ConnRouteParams.setDefaultProxy(params, new HttpHost(proxy.getHTTPSProxyHost(host), proxy.getHTTPSProxyPort(host)));
                    }
                }
                if("http".equals(this.getHost().getProtocol().getScheme())) {
                    if(proxy.isHTTPProxyEnabled(host)) {
                        ConnRouteParams.setDefaultProxy(params, new HttpHost(proxy.getHTTPProxyHost(host), proxy.getHTTPProxyPort(host)));
                    }
                }
            }
            ClientConnectionManager manager = new SingleClientConnManager(registry);
            http = new DefaultHttpClient(manager, params);
            this.configure(http);
        }
        return http;
    }

    protected void configure(AbstractHttpClient client) {
        http.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                log(true, request.getRequestLine().toString());
                for(Header header : request.getAllHeaders()) {
                    log(true, header.toString());
                }
            }
        });
        http.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                log(true, response.getStatusLine().toString());
                for(Header header : response.getAllHeaders()) {
                    log(false, header.toString());
                }
            }
        });
//        client.addRequestInterceptor(new GzipRequestInterceptor());
//        client.addResponseInterceptor(new GzipResponseInterceptor());
    }

    @Override
    public void close() {
        try {
            // When HttpClient instance is no longer needed, shut down the connection manager to ensure
            // immediate deallocation of all system resources
            if(null != http) {
                http.getConnectionManager().shutdown();
            }
        }
        finally {
            http = null;
        }
    }
}
