package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.CertificateStoreFactory;
import ch.cyberduck.core.Controller;

public class KeychainX509TrustManager extends CertificateStoreX509TrustManager implements X509TrustManager {

    public KeychainX509TrustManager(final TrustManagerHostnameCallback callback) {
        super(callback, CertificateStoreFactory.get());
    }

    public KeychainX509TrustManager(final TrustManagerHostnameCallback callback, final Controller controller) {
        super(callback, CertificateStoreFactory.get(controller));
    }

    public KeychainX509TrustManager(final TrustManagerHostnameCallback callback, final CertificateStore store) {
        super(callback, store);
    }
}