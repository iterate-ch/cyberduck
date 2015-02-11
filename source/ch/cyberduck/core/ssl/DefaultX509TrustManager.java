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
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public final class DefaultX509TrustManager implements X509TrustManager {
    private static final Logger log = Logger.getLogger(DefaultX509TrustManager.class);

    private X509TrustManager standardTrustManager;

    public DefaultX509TrustManager init() throws IOException {
        try {
            final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(KeyStore.getInstance(KeyStore.getDefaultType()));
            TrustManager[] trustmanagers = factory.getTrustManagers();
            if(trustmanagers.length == 0) {
                throw new NoSuchAlgorithmException("SunX509 trust manager not supported");
            }
            this.standardTrustManager = (X509TrustManager) trustmanagers[0];
        }
        catch(NoSuchAlgorithmException | KeyStoreException e) {
            throw new IOException(e);
        }
        return this;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {
        this.standardTrustManager.checkClientTrusted(x509Certificates, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {
        if((x509Certificates != null)) {
            if(log.isInfoEnabled()) {
                log.info("Server certificate chain:");
                for(int i = 0; i < x509Certificates.length; i++) {
                    log.info(String.format("X509Certificate[%d]=%s", i, x509Certificates[i]));
                }
            }
        }
        if((x509Certificates != null) && (x509Certificates.length == 1)) {
            x509Certificates[0].checkValidity();
        }
        else {
            standardTrustManager.checkServerTrusted(x509Certificates, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return standardTrustManager.getAcceptedIssuers();
    }
}