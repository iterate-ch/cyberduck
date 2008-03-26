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

import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Keychain;

import org.apache.log4j.Logger;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public class KeychainX509TrustManager extends AbstractX509TrustManager {
    protected static Logger log = Logger.getLogger(KeychainX509TrustManager.class);

    /**
     * All X509 certificates accepted by the user or found in the Keychain
     */
    protected List acceptedCertificates;

    public KeychainX509TrustManager() {
        this.acceptedCertificates = new Collection();
        try {
            this.init(KeyStore.getInstance(KeyStore.getDefaultType()));
        }
        catch(NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        catch(KeyStoreException e) {
            log.error(e.getMessage());
        }
    }

    private void acceptCertificate(final X509Certificate[] certs) {
        if(log.isInfoEnabled()) {
            log.info("Certificate trusted:" + certs.toString());
        }
        acceptedCertificates.addAll(Arrays.asList(certs));
    }

    private void acceptCertificate(final X509Certificate cert) {
        if(log.isInfoEnabled()) {
            log.info("Certificate trusted:" + cert.toString());
        }
        acceptedCertificates.add(cert);
    }

    public void checkClientTrusted(final X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        for(int i = 0; i < x509Certificates.length; i++) {
            this.checkCertificate(x509Certificates[i]);
        }
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        for(int i = 0; i < x509Certificates.length; i++) {
            this.checkCertificate(x509Certificates[i]);
        }
    }

    public void checkCertificate(final X509Certificate cert)
            throws CertificateException {

        try {
            if(Keychain.instance().isTrusted(cert.getEncoded())) {
                log.info("Certificate trusted in Keychain");
                // We still accept the certificate if we find it in the Keychain
                // regardless of its trust settings. There is currently no way I am
                // aware of to read the trust settings for a certificate in the Keychain
                this.acceptCertificate(cert);
            }
        }
        catch(CertificateException c) {
            log.error("Error getting certificate from the keychain: " + c.getMessage());
        }
        if(!acceptedCertificates.contains(cert)) {
            // The certificate has not been trusted
            throw new CertificateException(
                    NSBundle.localizedString("No trusted certificate found", "Status", ""));
        }
    }

    /**
     * @return All accepted certificates
     */
    public X509Certificate[] getAcceptedIssuers() {
        return (X509Certificate[]) this.acceptedCertificates.toArray(
                new X509Certificate[this.acceptedCertificates.size()]);
    }
}