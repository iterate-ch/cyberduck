package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public final class ThreadLocalHostnameDelegatingTrustManager implements X509TrustManager, TrustManagerHostnameCallback {
    private static final Logger log = Logger.getLogger(ThreadLocalHostnameDelegatingTrustManager.class);

    /**
     * Target hostname of current request stored as thread local
     */
    private ThreadLocal<String> target
            = new ThreadLocal<String>();

    private X509TrustManager delegate;

    /**
     * Lax hostname verification
     */
    private final boolean strict;

    public ThreadLocalHostnameDelegatingTrustManager(final X509TrustManager delegate, final String hostname) {
        this(delegate, hostname, false);
    }

    public ThreadLocalHostnameDelegatingTrustManager(final X509TrustManager delegate, final String hostname, final boolean strict) {
        this.delegate = delegate;
        this.strict = strict;
        this.setTarget(hostname);
    }

    @Override
    public X509TrustManager init() throws IOException {
        delegate.init();
        return this;
    }

    @Override
    public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
        delegate.verify(hostname, certs, cipher);
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {
        delegate.verify(target.get(), certs, cipher);
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {
        delegate.verify(target.get(), certs, cipher);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return delegate.getAcceptedIssuers();
    }

    @Override
    public String getTarget() {
        return target.get();
    }

    public void setTarget(final String hostname) {
        if(strict) {
            this.target.set(hostname);
        }
        else {
            final String simple;
            final String[] parts = StringUtils.split(hostname, '.');
            if(parts.length > 4) {
                ArrayUtils.reverse(parts);
                // Rewrite c.cyberduck.s3.amazonaws.com which does not match wildcard certificate *.s3.amazonaws.com
                simple = StringUtils.join(parts[3], ".", parts[2], ".", parts[1], ".", parts[0]);
                log.warn(String.format("Rewrite hostname target to %s", simple));
            }
            else {
                simple = hostname;
            }
            this.target.set(simple);
        }
    }
}
