package ch.cyberduck.core.ssl;

import ch.cyberduck.core.Collection;

import org.apache.log4j.Logger;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

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

/**
 * @version $Id$
 */
public class IgnoreX509TrustManager implements X509TrustManager {
    private static Logger log = Logger.getLogger(IgnoreX509TrustManager.class);

    protected List acceptedCertificates;

    public IgnoreX509TrustManager() {
        this.acceptedCertificates = new Collection();
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, java.lang.String string)
            throws java.security.cert.CertificateException {
        log.warn("Certificate not verified!");
        acceptedCertificates.addAll(Arrays.asList(x509Certificates));
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, java.lang.String string)
            throws java.security.cert.CertificateException {
        log.warn("Certificate not verified!");
        acceptedCertificates.addAll(Arrays.asList(x509Certificates));
    }

    /**
     * @return All accepted certificates
     */
    public X509Certificate[] getAcceptedIssuers() {
        return (X509Certificate[]) this.acceptedCertificates.toArray(
                new X509Certificate[this.acceptedCertificates.size()]);
    }
}