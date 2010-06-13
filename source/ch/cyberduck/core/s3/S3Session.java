package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.http.StickyHostConfiguration;
import ch.cyberduck.core.s3h.S3HSession;
import ch.cyberduck.core.ssl.*;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;

import java.io.IOException;
import java.util.List;

/**
 * @version $Id$
 */
public class S3Session extends S3HSession implements SSLSession {

    static {
        SessionFactory.addFactory(Protocol.S3_SSL, new Factory());
    }

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new S3Session(h);
        }
    }

    protected S3Session(Host h) {
        super(h);
    }

    private AbstractX509TrustManager trustManager;

    /**
     * @return
     */
    public AbstractX509TrustManager getTrustManager() {
        if(null == trustManager) {
            if(Preferences.instance().getBoolean("s3.tls.acceptAnyCertificate")) {
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

    @Override
    protected void login(final Credentials credentials) throws IOException {
        final HostConfiguration hostconfig = new StickyHostConfiguration();
        hostconfig.setHost(host.getHostname(), host.getPort(),
                new org.apache.commons.httpclient.protocol.Protocol(host.getProtocol().getScheme(),
                        (ProtocolSocketFactory) new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()), host.getPort())
        );
        this.login(credentials, hostconfig);
    }

    @Override
    protected List<S3Bucket> getBuckets(boolean reload) throws IOException, S3ServiceException {
        if(!host.getCredentials().isAnonymousLogin()) {
            // List all operation
            this.getTrustManager().setHostname(host.getHostname());
        }
        return super.getBuckets(reload);
    }

    @Override
    protected S3Bucket getBucket(final String container) throws IOException {
        final S3Bucket bucket = super.getBucket(container);
        // We now connect to bucket subdomain
        this.getTrustManager().setHostname(
                this.getHostnameForBucket(bucket.getName()));
        return bucket;
    }
}
