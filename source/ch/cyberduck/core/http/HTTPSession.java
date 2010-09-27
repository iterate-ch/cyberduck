package ch.cyberduck.core.http;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.ssl.AbstractX509TrustManager;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.IgnoreX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public abstract class HTTPSession extends Session implements SSLSession {

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
                HTTPSession.this.log(false, StringUtils.remove(m, IN));
            }
            else if(m.startsWith(OUT)) {
                HTTPSession.this.log(true, StringUtils.remove(m, OUT));
            }
        }
    };

    protected HTTPSession(Host h) {
        super(h);
    }

    /**
     * @return
     */
    public AbstractX509TrustManager getTrustManager() {
        return new IgnoreX509TrustManager();
    }

    /**
     * SSL socket factory using parameters from HTTP connection configuration.
     */
    protected static class SocketFactory extends CustomTrustSSLProtocolSocketFactory
            implements ProtocolSocketFactory {

        public SocketFactory(AbstractX509TrustManager manager) {
            super(manager);
        }

        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
                                   org.apache.commons.httpclient.params.HttpConnectionParams params)
                throws IOException {
            SSLSocketFactory factory = this.getSSLContext().getSocketFactory();
            Socket socket = factory.createSocket(host, port, localAddress, localPort);
            socket.setTcpNoDelay(params.getTcpNoDelay());
            socket.setSoTimeout(params.getSoTimeout());
            return socket;
        }
    }

    /**
     * Create a sticky host configuration with a socket factory for the given scheme
     *
     * @return A host configuration initialized with the hostname, port and socket factory.
     */
    protected HostConfiguration getHostConfiguration() {
        return this.getHostConfiguration(URI.create(host.toURL()));
    }

    protected HostConfiguration getHostConfiguration(URI uri) {
        final HostConfiguration configuration = new StickyHostConfiguration();
        int port = uri.getPort();
        if(this.getHost().getProtocol().isSecure()) {
            if(-1 == port) {
                port = 443;
            }
            // Configuration with custom socket factory using the trust manager
            configuration.setHost(uri.getHost(), port,
                    new org.apache.commons.httpclient.protocol.Protocol(uri.getScheme(),
                            new SocketFactory(this.getTrustManager()), port)
            );
            if(Preferences.instance().getBoolean("connection.proxy.enable")) {
                final Proxy proxy = ProxyFactory.instance();
                if(proxy.isHTTPSProxyEnabled()) {
                    configuration.setProxy(proxy.getHTTPSProxyHost(), proxy.getHTTPSProxyPort());
                }
            }
        }
        else {
            if(-1 == port) {
                port = 80;
            }
            configuration.setHost(uri.getHost(), port,
                    new org.apache.commons.httpclient.protocol.Protocol(uri.getScheme(),
                            new DefaultProtocolSocketFactory(), port)
            );
            if(Preferences.instance().getBoolean("connection.proxy.enable")) {
                final Proxy proxy = ProxyFactory.instance();
                if(proxy.isHTTPProxyEnabled()) {
                    configuration.setProxy(proxy.getHTTPProxyHost(), proxy.getHTTPProxyPort());
                }
            }
        }
        final HostParams parameters = configuration.getParams();
        parameters.setParameter(HttpMethodParams.USER_AGENT, this.getUserAgent());
        parameters.setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        parameters.setParameter(HttpMethodParams.SO_TIMEOUT, this.timeout());
        return configuration;
    }

    @Override
    protected void fireConnectionWillOpenEvent() throws ResolveCanceledException, UnknownHostException {
        // For 3.x
        Logger.getLogger("httpclient.wire.header").addAppender(appender);
        // For HTTP Components 4
        Logger.getLogger("org.apache.http.headers").addAppender(appender);
        super.fireConnectionWillOpenEvent();
    }

    @Override
    protected void fireConnectionWillCloseEvent() {
        // For 3.x
        Logger.getLogger("httpclient.wire.header").removeAppender(appender);
        // For HTTP Components 4
        Logger.getLogger("org.apache.http.headers").removeAppender(appender);
        super.fireConnectionWillCloseEvent();
    }
}
