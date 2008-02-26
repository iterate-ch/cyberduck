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
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.IgnoreX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.commons.httpclient.HttpClient;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * @version $Id:$
 */
public class DAVSSession extends DAVSession implements SSLSession {

    static {
        SessionFactory.addFactory(Protocol.WEBDAV_SSL, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new DAVSSession(h);
        }
    }

    protected DAVSSession(Host h) {
        super(h);
    }

    protected void configure() throws IOException {
        super.configure();
        final HttpClient client = this.DAV.getSessionInstance(this.DAV.getHttpURL(), false);
        client.getHostConfiguration().setHost(host.getHostname(), host.getPort(),
                new org.apache.commons.httpclient.protocol.Protocol("https",
                        new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()), host.getPort()));
        if(Proxy.isHTTPSProxyEnabled()) {
            this.DAV.setProxy(Proxy.getHTTPSProxyHost(), Proxy.getHTTPSProxyPort());
        }
    }

    /**
     * A trust manager accepting any certificate by default
     */
    private X509TrustManager trustManager
            = new IgnoreX509TrustManager();

    /**
     * @return
     */
    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    /**
     * Override the default ignoring trust manager
     *
     * @param trustManager
     */
    public void setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    /**
     * @return
     */
    public String getSecurityInformation() {
        StringBuffer info = new StringBuffer();
        X509Certificate[] accepted = this.getTrustManager().getAcceptedIssuers();
        for(int i = 0; i < accepted.length; i++) {
            info.append(accepted[i].toString());
        }
        return info.toString();
    }
}
