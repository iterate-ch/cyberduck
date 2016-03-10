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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class DisabledX509TrustManager extends AbstractX509TrustManager {
    @Override
    public void checkClientTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {
        this.accept(Arrays.asList(certs));
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {
        this.accept(Arrays.asList(certs));
    }

    @Override
    public X509TrustManager init() {
        return this;
    }

    @Override
    public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
        this.accept(Arrays.asList(certs));
    }
}
