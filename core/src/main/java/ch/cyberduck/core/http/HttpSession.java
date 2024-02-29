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
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

public abstract class HttpSession<C> extends SSLSession<C> {

    protected final HttpConnectionPoolBuilder builder;

    protected HttpSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
        this.builder = new HttpConnectionPoolBuilder(host,
            trust instanceof ThreadLocalHostnameDelegatingTrustManager ? (ThreadLocalHostnameDelegatingTrustManager) trust :
                new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, ProxyFactory.get());
    }
}
