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
import java.util.*;

/**
 * @version $Id$
 */
public abstract class SSLSession extends Session {

    protected SSLSession(Host h) {
        super(h);
    }

    /**
     *
     */
    protected Map<String, AbstractX509TrustManager> trust
            = new HashMap<String, AbstractX509TrustManager>();

    public AbstractX509TrustManager getTrustManager() {
        return this.getTrustManager(host.getHostname());
    }

    public AbstractX509TrustManager getTrustManager(final String hostname) {
        if(!trust.containsKey(hostname)) {
            trust.put(hostname, new KeychainX509TrustManager() {
                @Override
                public String getHostname() {
                    return hostname;
                }
            });
        }
        return trust.get(hostname);
    }

    /**
     * @return List of certificates accepted by all trust managers of this session.
     */
    public List<X509Certificate> getAcceptedIssuers() {
        List<X509Certificate> accepted = new ArrayList<X509Certificate>();
        for(AbstractX509TrustManager m : trust.values()) {
            accepted.addAll(Arrays.asList(m.getAcceptedIssuers()));
        }
        return accepted;
    }

    @Override
    protected void fireConnectionDidCloseEvent() {
        trust.clear();
        super.fireConnectionDidCloseEvent();
    }
}
