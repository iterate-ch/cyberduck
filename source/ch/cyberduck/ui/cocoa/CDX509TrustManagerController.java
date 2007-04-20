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

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.ftps.AbstractX509TrustManager;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSCell;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSTextView;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
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
        }
        catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        catch (KeyStoreException e) {
            log.error(e.getMessage());
        }
    }

    public void checkClientTrusted(final X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        this.checkCertificate(x509Certificates, authType);
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        this.checkCertificate(x509Certificates, authType);
    }

    public void checkCertificate(final X509Certificate[] x509Certificates, String authType)
            throws CertificateException {
        for (int i = 0; i < x509Certificates.length; i++) {
            final X509Certificate cert = x509Certificates[i];
            try {
                if (!this.acceptedCertificates.contains(cert)) {
                    // The certificate has not been verified before
                    super.checkServerTrusted(x509Certificates, authType);
                }
            }
            catch (final CertificateException e) {
                if (this.keychainKnowsAbout(x509Certificates[i])) {
                    // We still accept the certificate if we find it in the Keychain
                    // regardless of its trust settings. There is currently no way I am
                    // aware of to read the trust settings for a certificate in the Keychain
                    acceptedCertificates.add(cert);
                    return;
                }
                CDSheetController c = new CDSheetController(parent) {
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
                                    +": "+e.getMessage());
                        }
                        catch(CertificateExpiredException e) {
                            log.warn(e.getMessage());
                            this.alertIcon.setHidden(false);
                            this.validityField.setStringValue(NSBundle.localizedString("Certificate expired", "")
                                    +": "+e.getMessage());
                        }
                    }

                    private NSTextField alertField; // IBOutlet

                    public void setAlertField(NSTextField alertField) {
                        this.alertField = alertField;
                        this.alertField.setStringValue(e.getMessage());
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
                        if (returncode == DEFAULT_OPTION) { //Allow
                            acceptedCertificates.add(cert);
                            if (alwaysButton.state() == NSCell.OnState) {
                                saveToKeychain(cert);
                            }
                        }
                    }

                    private void saveToKeychain(X509Certificate cert) {
                        try {
                            Keychain.instance().addCertificateToKeychain(cert.getEncoded());
                            log.info("Certificate added to Keychain");
                        }
                        catch (CertificateEncodingException e) {
                            log.error(e.getMessage());
                        }
                    }
                };
                synchronized(NSApplication.sharedApplication()) {
                    if (!NSApplication.loadNibNamed("Certificate", c)) {
                        log.fatal("Couldn't load Certificate.nib");
                    }
                }
                c.beginSheet(true);
                if (!acceptedCertificates.contains(cert)) {
                    // The certificate has not been trusted
                    throw new CertificateException(NSBundle.localizedString("No trusted certificate found", "Status", ""));
                }
            }
        }
    }

    /**
     * @return All accepted certificates
     */
    public X509Certificate[] getAcceptedIssuers() {
        return (X509Certificate[])this.acceptedCertificates.toArray(
                new X509Certificate[this.acceptedCertificates.size()]);
    }

    public boolean keychainKnowsAbout(X509Certificate certificate) {
        try {
            if (Keychain.instance().hasCertificate(certificate.getEncoded())) {
                log.info("Certificate exists in Keychain");
                return true;
            }
        }
        catch (CertificateException e) {
            log.error("Error getting certificate from the keychain: " + e.getMessage());
        }
        log.info("Certificate not found in Keychain");
        return false;
    }
}
