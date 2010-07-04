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

import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public class IgnoreX509TrustManager extends AbstractX509TrustManager {
    private static Logger log = Logger.getLogger(IgnoreX509TrustManager.class);

    public IgnoreX509TrustManager() {
        super(null);
    }

    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType)
            throws java.security.cert.CertificateException {
        log.warn("Certificate not verified!");
        this.acceptCertificate(x509Certificates);
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws java.security.cert.CertificateException {
        log.warn("Certificate not verified!");
        this.acceptCertificate(x509Certificates);
    }
}