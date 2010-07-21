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
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
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
            final String m = StringUtils.remove(event.getMessage().toString(), "[\\r][\\n]");
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
     * Create a sticky host configuration with a socket factory for the given scheme
     *
     * @return A host configuration initialized with the hostname, port and socket factory.
     */
    protected HostConfiguration getHostConfiguration() {
        final HostConfiguration configuration = new StickyHostConfiguration();
        final Proxy proxy = ProxyFactory.instance();
        if(this.getHost().getProtocol().isSecure()) {
            // Configuration with custom socket factory using the trust manager
            configuration.setHost(host.getHostname(), host.getPort(),
                    new org.apache.commons.httpclient.protocol.Protocol(host.getProtocol().getScheme(),
                            (ProtocolSocketFactory) new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()), host.getPort())
            );
            if(proxy.isHTTPSProxyEnabled()) {
                configuration.setProxy(proxy.getHTTPSProxyHost(), proxy.getHTTPSProxyPort());
            }
        }
        else {
            configuration.setHost(host.getHostname(), host.getPort(),
                    new org.apache.commons.httpclient.protocol.Protocol(host.getProtocol().getScheme(),
                            new DefaultProtocolSocketFactory(), host.getPort())
            );
            if(proxy.isHTTPProxyEnabled()) {
                configuration.setProxy(proxy.getHTTPProxyHost(), proxy.getHTTPProxyPort());
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
