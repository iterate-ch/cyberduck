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
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;

import javax.net.SocketFactory;

public abstract class HttpSession<C> extends SSLSession<C> {

    protected HttpConnectionPoolBuilder builder;

    protected HttpSession(final Host host, final ThreadLocalHostnameDelegatingTrustManager trust, final X509KeyManager key) {
        this(host, trust, key, ProxyFactory.get());
    }

    protected HttpSession(final Host host, final ThreadLocalHostnameDelegatingTrustManager trust, final X509KeyManager key, final ProxyFinder proxyFinder) {
        super(host, trust, key);
        this.builder = new HttpConnectionPoolBuilder(host, trust, key, proxyFinder);
    }

    protected HttpSession(final Host host, final ThreadLocalHostnameDelegatingTrustManager trust, final X509KeyManager key, final SocketFactory socketFactory) {
        super(host, trust, key);
        this.builder = new HttpConnectionPoolBuilder(host, trust, key, ProxyFactory.get(), socketFactory);
    }

    public void setBuilder(final HttpConnectionPoolBuilder builder) {
        this.builder = builder;
    }

    public HttpConnectionPoolBuilder getBuilder() {
        return builder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(Class<T> type) {
        if(type == Upload.class) {
            return (T) new HttpUploadFeature((AbstractHttpWriteFeature<?>) this.getFeature(Write.class));
        }
        return super.getFeature(type);
    }
}