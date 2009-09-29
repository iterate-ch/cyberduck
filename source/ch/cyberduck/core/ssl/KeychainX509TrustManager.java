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

import ch.cyberduck.core.KeychainFactory;
import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public class KeychainX509TrustManager extends AbstractX509TrustManager {
    protected static Logger log = Logger.getLogger(KeychainX509TrustManager.class);

    public KeychainX509TrustManager(String hostname) {
        super(hostname);
    }

    public void checkClientTrusted(final X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        this.checkCertificates(x509Certificates);
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        this.checkCertificates(x509Certificates);
    }

    private void checkCertificates(final X509Certificate[] certs)
            throws CertificateException {

        if(KeychainFactory.instance().isTrusted(this.getHostname(), certs)) {
            log.info("Certificate trusted in Keychain");
            // We still accept the certificate if we find it in the Keychain
            // regardless of its trust settings. There is currently no way I am
            // aware of to read the trust settings for a certificate in the Keychain
            this.acceptCertificate(certs);
            return;
        }
        // The certificate has not been trusted
        throw new CertificateException(
                Locale.localizedString("No trusted certificate found", "Status"));
    }

}