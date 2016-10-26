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

import org.apache.log4j.Logger;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation for certificate trust settings.

 */
public abstract class AbstractX509TrustManager implements X509TrustManager {
    private static final Logger log = Logger.getLogger(AbstractX509TrustManager.class);

    /**
     * A set of all X509 certificates accepted by the user that contains
     * no duplicate elements
     */
    private final Set<X509Certificate> accepted
            = Collections.synchronizedSet(new LinkedHashSet<X509Certificate>());

    protected void accept(final List<X509Certificate> certs) {
        if(log.isTraceEnabled()) {
            for(X509Certificate cert : certs) {
                log.trace(String.format("Certificate %s trusted", cert.toString()));
            }
        }
        accepted.clear();
        accepted.addAll(certs);
    }

    /**
     * @return All accepted certificates
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return accepted.toArray(new X509Certificate[accepted.size()]);
    }
}
