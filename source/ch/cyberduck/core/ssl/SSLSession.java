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

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.X509TrustManager;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class SSLSession<C> extends Session<C> implements TrustManagerHostnameCallback {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private X509TrustManager trust;

    protected SSLSession(final Host h) {
        super(h);
        this.trust = new KeychainX509TrustManager(this);
    }

    protected SSLSession(final Host host, final X509TrustManager manager) {
        super(host);
        this.trust = manager;
    }

    public String getTarget() {
        return new PunycodeConverter().convert(host.getHostname());
    }

    /**
     * @return Trust manager backed by keychain
     */
    public X509TrustManager getTrustManager() {
        return trust;
    }

    /**
     * @return List of certificates accepted by all trust managers of this session.
     */
    public List<X509Certificate> getAcceptedIssuers() {
        return Arrays.asList(trust.getAcceptedIssuers());
    }
}
