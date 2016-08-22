package ch.cyberduck.core.ssl;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class DefaultX509TrustManager extends AbstractX509TrustManager {
    private static final Logger log = Logger.getLogger(DefaultX509TrustManager.class);

    private javax.net.ssl.X509TrustManager system;

    public DefaultX509TrustManager init() throws IOException {
        try {
            final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(KeyStore.getInstance(KeyStore.getDefaultType()));
            final TrustManager[] trustmanagers = factory.getTrustManagers();
            if(trustmanagers.length == 0) {
                throw new NoSuchAlgorithmException("SunX509 trust manager not supported");
            }
            system = (javax.net.ssl.X509TrustManager) trustmanagers[0];
        }
        catch(NoSuchAlgorithmException | KeyStoreException e) {
            log.error(String.format("Initialization of trust store failed. %s", e.getMessage()));
            throw new IOException(e);
        }
        return this;
    }

    @Override
    public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
        certs[0].checkValidity();
        this.accept(Arrays.asList(certs));
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {
        system.checkClientTrusted(certs, cipher);
        this.accept(Arrays.asList(certs));
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] certs, final String cipher) throws CertificateException {
        if((certs != null)) {
            if(log.isDebugEnabled()) {
                log.debug("Server certificate chain:");
                for(int i = 0; i < certs.length; i++) {
                    log.debug(String.format("X509Certificate[%d]=%s", i, certs[i]));
                }
            }
        }
        if((certs != null) && (certs.length == 1)) {
            this.verify(null, certs, cipher);
        }
        else {
            system.checkServerTrusted(certs, cipher);
        }
    }
}