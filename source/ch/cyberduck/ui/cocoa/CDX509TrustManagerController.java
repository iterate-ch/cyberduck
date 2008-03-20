package ch.cyberduck.ui.cocoa;

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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.ssl.AbstractX509TrustManager;

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
public class CDX509TrustManagerController extends AbstractX509TrustManager {
    protected static Logger log = Logger.getLogger(CDX509TrustManagerController.class);

    private CDWindowController parent;

    /**
     * All X509 certificates accepted by the user or found in the Keychain
     */
    protected List acceptedCertificates;

    public CDX509TrustManagerController(CDWindowController windowController) {
        this.parent = windowController;
        this.acceptedCertificates = new Collection();
        try {
            this.init(KeyStore.getInstance(KeyStore.getDefaultType()));
//            try {
//                keychain = KeyStore.getInstance("KeychainStore", "Apple");
//                try {
//                    keychain.load(null);
//                }
//                catch(IOException e) {
//                    throw new NoSuchProviderException(e.getMessage());
//
//                }
//                catch(CertificateException e) {
//                    throw new NoSuchProviderException(e.getMessage());
//                }
//                this.init(keychain);
//            }
//            catch(NoSuchProviderException e) {
//                log.error(e.getMessage());
//                this.init(keychain = KeyStore.getInstance(KeyStore.getDefaultType()));
//            }
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

        try {
            super.checkClientTrusted(x509Certificates, authType);
            this.acceptCertificate(x509Certificates);
        }
        catch(final CertificateException e) {
            for(int i = 0; i < x509Certificates.length; i++) {
                this.checkCertificate(x509Certificates[i], e);
            }
        }
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        try {
            super.checkServerTrusted(x509Certificates, authType);
            this.acceptCertificate(x509Certificates);
        }
        catch(final CertificateException e) {
            for(int i = 0; i < x509Certificates.length; i++) {
                this.checkCertificate(x509Certificates[i], e);
            }
        }
    }

    public void checkCertificate(final X509Certificate cert, final CertificateException e)
            throws CertificateException {

        try {
            if(Keychain.instance().hasCertificate(cert.getEncoded())) {
                log.info("Certificate found in Keychain");
                // We still accept the certificate if we find it in the Keychain
                // regardless of its trust settings. There is currently no way I am
                // aware of to read the trust settings for a certificate in the Keychain
                acceptedCertificates.add(cert);
                return;
            }
        }
        catch(CertificateException c) {
            log.error("Error getting certificate from the keychain: " + c.getMessage());
        }
        log.info("Certificate not found in Keychain");
//        try {
//            // The (alias) name of the first entry with matching
//            // certificate, or null if no such entry exists in this keystore.
//            final String alias = keychain.getCertificateAlias(cert);
//            if(alias != null) {
//                if(log.isInfoEnabled()) {
//                    log.info("Accepted certificate " + cert.toString());
//                }
//                // We still accept the certificate if we find it in the Keychain
//                // regardless of its trust settings. There is currently no way I am
//                // aware of to read the trust settings for a certificate in the Keychain
//                this.acceptCertificate(cert);
//                return;
//            }
//        }
//        catch(KeyStoreException k) {
//            log.error(k.getMessage());
//        }
        CDSheetController c = new CDSheetController(parent) {
            protected String getBundleName() {
                return "Certificate";
            }

            private NSButton alertIcon; // IBOutlet

            public void setAlertIcon(NSButton alertIcon) {
                this.alertIcon = alertIcon;
                this.alertIcon.setHidden(true);
            }

            private NSTextField validityField; // IBOutlet

            public void setValidityField(NSTextField validityField) {
                this.validityField = validityField;
                try {
                    cert.checkValidity();
                }
                catch(CertificateNotYetValidException e) {
                    log.warn(e.getMessage());
                    this.alertIcon.setHidden(false);
                    this.validityField.setStringValue(NSBundle.localizedString("Certificate not yet valid", "")
                            + ": " + e.getMessage());
                }
                catch(CertificateExpiredException e) {
                    log.warn(e.getMessage());
                    this.alertIcon.setHidden(false);
                    this.validityField.setStringValue(NSBundle.localizedString("Certificate expired", "")
                            + ": " + e.getMessage());
                }
            }

            private NSTextField alertField; // IBOutlet

            public void setAlertField(NSTextField alertField) {
                this.alertField = alertField;
                this.updateField(this.alertField, e.getMessage());
            }

            private NSTextView certificateField; // IBOutlet

            public void setCertificateField(NSTextView certificateField) {
                this.certificateField = certificateField;
                this.certificateField.setString(cert.toString());
            }

            private NSButton alwaysButton; // IBOutlet

            public void setAlwaysButton(NSButton alwaysButton) {
                this.alwaysButton = alwaysButton;
                this.alwaysButton.setEnabled(true);
            }

            public void callback(final int returncode) {
                if(returncode == DEFAULT_OPTION) { //Allow
                    acceptCertificate(cert);
                    if(alwaysButton.state() == NSCell.OnState) {
                        try {
                            Keychain.instance().addCertificateToKeychain(cert.getEncoded());
                            log.info("Certificate added to Keychain");
                        }
                        catch(CertificateEncodingException e) {
                            log.error(e.getMessage());
                        }
                    }
                }
            }
        };
        c.beginSheet();
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