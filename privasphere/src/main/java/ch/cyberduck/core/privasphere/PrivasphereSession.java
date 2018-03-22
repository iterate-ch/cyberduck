package ch.cyberduck.core.privasphere;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.http.RedirectCallback;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import javax.net.SocketFactory;

public class PrivasphereSession extends DAVSession {
    public PrivasphereSession(final Host host) {
        super(host);
    }

    public PrivasphereSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public PrivasphereSession(final Host host, final RedirectCallback redirect) {
        super(host, redirect);
    }

    public PrivasphereSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final RedirectCallback redirect) {
        super(host, trust, key, redirect);
    }

    public PrivasphereSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final SocketFactory socketFactory) {
        super(host, trust, key, socketFactory);
    }

    public PrivasphereSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final SocketFactory socketFactory, final RedirectCallback redirect) {
        super(host, trust, key, socketFactory, redirect);
    }
}
