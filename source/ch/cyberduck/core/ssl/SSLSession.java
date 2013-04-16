package ch.cyberduck.core.ssl;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class SSLSession extends Session {

    private AbstractX509TrustManager trustManager;

    protected SSLSession(Host h) {
        super(h);
        this.trustManager = new KeychainX509TrustManager() {
            @Override
            public String getHostname() {
                return SSLSession.this.getDomain();
            }
        };
    }

    protected String getDomain() {
        return this.getHost().getHostname(true);
    }

    /**
     * @return Trust manager backed by keychain
     */
    public AbstractX509TrustManager getTrustManager() {
        return trustManager;
    }

    /**
     * @return List of certificates accepted by all trust managers of this session.
     */
    public List<X509Certificate> getAcceptedIssuers() {
        return Arrays.asList(trustManager.getAcceptedIssuers());
    }
}
