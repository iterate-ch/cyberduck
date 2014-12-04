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
import ch.cyberduck.core.idna.PunycodeConverter;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class SSLSession<C> extends Session<C> implements TrustManagerHostnameCallback {
    private static final Logger log = Logger.getLogger(SSLSession.class);

    static {
        Security.insertProviderAt(new BouncyCastleProvider(),
                PreferencesFactory.get().getInteger("connection.ssl.provider.bouncycastle.position"));
    }

    protected X509TrustManager trust;

    protected X509KeyManager key;

    protected SSLSession(final Host h) {
        super(h);
        this.trust = new KeychainX509TrustManager(this);
        this.key = new KeychainX509KeyManager();
    }

    protected SSLSession(final Host host, final X509TrustManager manager) {
        super(host);
        this.trust = manager;
        this.key = new KeychainX509KeyManager();
    }

    protected SSLSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h);
        this.trust = trust;
        this.key = key;
    }

    public String getTarget() {
        return new PunycodeConverter().convert(host.getHostname());
    }

    /**
     * @return Trust manager backed by keychain
     */
    public X509TrustManager getTrustManager() {
        try {
            return trust.init();
        }
        catch(IOException e) {
            log.error(String.format("Initialization of trust store failed %s", e.getMessage()));
        }
        return trust;
    }

    public X509KeyManager getKeyManager() {
        try {
            return key.init();
        }
        catch(IOException e) {
            log.error(String.format("Initialization of key store failed %s", e.getMessage()));
        }
        return key;
    }

    /**
     * @return List of certificates accepted by all trust managers of this session.
     */
    public List<X509Certificate> getAcceptedIssuers() {
        return Arrays.asList(trust.getAcceptedIssuers());
    }
}
