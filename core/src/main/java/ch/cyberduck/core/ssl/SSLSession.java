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
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public abstract class SSLSession<C> extends Session<C> {
    private static final Logger log = Logger.getLogger(SSLSession.class);

    static {
        final int position = PreferencesFactory.get().getInteger("connection.ssl.provider.bouncycastle.position");
        final BouncyCastleProvider provider = new BouncyCastleProvider();
        if(log.isInfoEnabled()) {
            log.info(String.format("Install provider %s at position %d", provider, position));
        }
        Security.insertProviderAt(provider, position);
    }

    protected X509TrustManager trust;

    protected X509KeyManager key;

    protected SSLSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h);
        this.trust = trust;
        this.key = key;
    }

    @Override
    protected <T> T _getFeature(final Class<T> type) {
        if(type == X509TrustManager.class) {
            return (T) trust;
        }
        if(type == X509KeyManager.class) {
            return (T) key;
        }
        return super._getFeature(type);
    }
}
