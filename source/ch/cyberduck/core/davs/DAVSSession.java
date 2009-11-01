package ch.cyberduck.core.davs;

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
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.ssl.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import java.io.IOException;

/**
 * @version $Id$
 */
public class DAVSSession extends DAVSession implements SSLSession {

    static {
        SessionFactory.addFactory(Protocol.WEBDAV_SSL, new Factory());
    }

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new DAVSSession(h);
        }
    }

    protected DAVSSession(Host h) {
        super(h);
    }

    @Override
    protected void configure() throws IOException {
        super.configure();
        final HttpClient client = this.DAV.getSessionInstance(this.DAV.getHttpURL(), false);
        client.getHostConfiguration().setHost(host.getHostname(), host.getPort(),
                new org.apache.commons.httpclient.protocol.Protocol("https",
                        (ProtocolSocketFactory)new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()), host.getPort()));
        final Proxy proxy = ProxyFactory.instance();
        if(proxy.isHTTPSProxyEnabled()) {
            this.DAV.setProxy(proxy.getHTTPSProxyHost(), proxy.getHTTPSProxyPort());
            //this.DAV.setProxyCredentials(new UsernamePasswordCredentials(null, null));
        }
        else {
            this.DAV.setProxy(null, -1);
            //this.DAV.setProxyCredentials(null);
        }
    }

    private AbstractX509TrustManager trustManager;

    /**
     * @return
     */
    public AbstractX509TrustManager getTrustManager() {
        if(null == trustManager) {
            if(Preferences.instance().getBoolean("webdav.tls.acceptAnyCertificate")) {
                this.setTrustManager(new IgnoreX509TrustManager());
            }
            else {
                this.setTrustManager(new KeychainX509TrustManager(host.getHostname()));
            }
        }
        return trustManager;
    }

    /**
     * Override the default ignoring trust manager
     *
     * @param trustManager
     */
    private void setTrustManager(AbstractX509TrustManager trustManager) {
        this.trustManager = trustManager;
    }
}
