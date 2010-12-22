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

import ch.cyberduck.core.*;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import java.net.UnknownHostException;

/**
 * @version $Id: HTTPSession.java 7171 2010-10-02 15:06:28Z dkocher $
 */
public abstract class HTTP4Session extends SSLSession {

    private Appender appender = new AppenderSkeleton() {

        private static final String IN = "<< ";

        private static final String OUT = ">> ";

        public void close() {
            ;
        }

        public boolean requiresLayout() {
            return false;
        }

        @Override
        protected void append(LoggingEvent event) {
            final String m = StringUtils.remove(StringUtils.remove(event.getMessage().toString(), "[\\r][\\n]"), "\"");
            if(m.startsWith(IN)) {
                HTTP4Session.this.log(false, StringUtils.remove(m, IN));
            }
            else if(m.startsWith(OUT)) {
                HTTP4Session.this.log(true, StringUtils.remove(m, OUT));
            }
        }
    };

    protected HTTP4Session(Host h) {
        super(h);
    }

    private HttpClient http;

    /**
     * Create new HTTP client with default configuration and custom trust manager.
     *
     * @return A new instance of a default HTTP client.
     */
    protected HttpClient http() {
        if(null == http) {
            final HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, org.apache.http.HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, getEncoding());
            HttpProtocolParams.setUseExpectContinue(params, true);
            org.apache.http.params.HttpConnectionParams.setTcpNoDelay(params, true);
            org.apache.http.params.HttpConnectionParams.setSoTimeout(params, timeout());
            org.apache.http.params.HttpConnectionParams.setSocketBufferSize(params, 8192);
            params.setParameter(ClientPNames.MAX_REDIRECTS, 10);
            HttpProtocolParams.setUserAgent(params, getUserAgent());
            SchemeRegistry registry = new SchemeRegistry();
            if(host.getProtocol().isSecure()) {
                org.apache.http.conn.ssl.SSLSocketFactory factory = new SSLSocketFactory(new CustomTrustSSLProtocolSocketFactory(
                        getTrustManager()).getSSLContext());
                // We make sure to verify the hostname later using the trust manager
                factory.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                registry.register(
                        new Scheme(host.getProtocol().getScheme(), factory, host.getPort()));
            }
            else {
                registry.register(
                        new Scheme(host.getProtocol().getScheme(), PlainSocketFactory.getSocketFactory(), host.getPort()));
            }
            if("https".equals(this.getHost().getProtocol().getScheme())) {
                if(Preferences.instance().getBoolean("connection.proxy.enable")) {
                    final Proxy proxy = ProxyFactory.instance();
                    if(proxy.isHTTPSProxyEnabled(host)) {
                        params.setParameter(ConnRoutePNames.DEFAULT_PROXY,
                                new HttpHost(proxy.getHTTPSProxyHost(), proxy.getHTTPSProxyPort()));
                    }
                }
            }
            else if("http".equals(this.getHost().getProtocol().getScheme())) {
                if(Preferences.instance().getBoolean("connection.proxy.enable")) {
                    final Proxy proxy = ProxyFactory.instance();
                    if(proxy.isHTTPProxyEnabled(host)) {
                        params.setParameter(ConnRoutePNames.DEFAULT_PROXY,
                                new HttpHost(proxy.getHTTPProxyHost(), proxy.getHTTPProxyPort()));
                    }
                }
            }
            ClientConnectionManager manager = new SingleClientConnManager(params, registry);
            http = new DefaultHttpClient(manager, params);
        }
        return http;
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

    @Override
    protected void fireConnectionWillOpenEvent() throws ResolveCanceledException, UnknownHostException {
        // For HTTP Components 4
        Logger.getLogger("org.apache.http.headers").addAppender(appender);
        super.fireConnectionWillOpenEvent();
    }

    @Override
    protected void fireConnectionWillCloseEvent() {
        // For HTTP Components 4
        Logger.getLogger("org.apache.http.headers").removeAppender(appender);
        super.fireConnectionWillCloseEvent();
    }
}
