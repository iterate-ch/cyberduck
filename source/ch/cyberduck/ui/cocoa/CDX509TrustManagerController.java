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

import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.ftps.StandardX509TrustManager;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSAutoreleasePool;

import org.apache.log4j.Logger;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Vector;

/**
 * @version $Id$
 */
public class CDX509TrustManagerController
        extends CDSheetController implements javax.net.ssl.X509TrustManager {
    protected static Logger log = Logger.getLogger(CDX509TrustManagerController.class);

    public void setAlertField(NSTextField alertField) {
        this.alertField = alertField;
    }

    private NSTextField alertField;

    public void setCertificateField(NSTextView certificateField) {
        this.certificateField = certificateField;
    }

    private NSTextView certificateField;

    public void setAlwaysButton(NSButton alwaysButton) {
        this.alwaysButton = alwaysButton;
        this.alwaysButton.setEnabled(true);
    }

    private NSButton alwaysButton;

    public void setWindow(NSWindow window) {
        super.setWindow(window);
        this.window.setReleasedWhenClosed(false);
    }

    private StandardX509TrustManager delegate;

    protected List acceptedCertificates = new Vector();

    public CDX509TrustManagerController(CDWindowController parent) {
        super(parent);
        if (!NSApplication.loadNibNamed("Certificate", this)) {
            log.fatal("Couldn't load Certificate.nib");
        }
        try {
            this.delegate = new StandardX509TrustManager();
            this.delegate.init(KeyStore.getInstance(KeyStore.getDefaultType()));
        }
        catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        catch (KeyStoreException e) {
            log.error(e.getMessage());
        }
    }

    public void checkCertificate(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        for (int i = 0; i < x509Certificates.length; i++) {
            try {
                if (!this.acceptedCertificates.contains(x509Certificates[i])) {
                    delegate.checkServerTrusted(x509Certificates, authType);
                }
                this.acceptedCertificates.add(x509Certificates[i]);
            }
            catch (CertificateException e) {
                if (this.keychainKnowsAbout(x509Certificates[i])) {
                    return;
                }
                String cert = "";
                if (x509Certificates.length > 0) {
                    cert = x509Certificates[0].toString();
                }
                alertField.setStringValue(e.getMessage());
                certificateField.setString(cert);
                this.beginSheet(x509Certificates[0]);
                this.waitForSheetEnd();
                if (!this.acceptedCertificates.contains(x509Certificates[i])) {
                    throw e;
                }
            }
        }
    }

    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        this.checkCertificate(x509Certificates, authType);
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {

        this.checkCertificate(x509Certificates, authType);
    }

    public X509Certificate[] getAcceptedIssuers() {
        return delegate.getAcceptedIssuers();
    }

    public void dismissedSheet(int returncode, Object certificate) {
        if (returncode == NSAlertPanel.DefaultReturn) { //Allow
            this.acceptedCertificates.add(certificate);
            if (alwaysButton.state() == NSCell.OnState) {
                this.saveToKeychain((X509Certificate) certificate);
            }
        }
    }

    public void closeSheet(NSButton sender) {
        this.endSheet(sender.tag());
    }

    public boolean keychainKnowsAbout(X509Certificate certificate) {
        int pool = NSAutoreleasePool.push();
        try {
            if (Keychain.instance().hasCertificate(certificate.getEncoded())) {
                log.info("Certificate exists in Keychain");
                return true;
            }
        }
        catch (CertificateException e) {
            log.error("Error getting certificate from the keychain: " + e.getMessage());
        }
        finally {
            NSAutoreleasePool.pop(pool);
        }
        log.info("Certificate not found in Keychain");
        return false;
    }

    private void saveToKeychain(X509Certificate cert) {
        if (alwaysButton.state() == NSCell.OnState) {
            int pool = NSAutoreleasePool.push();
            try {
                Keychain.instance().addCertificateToKeychain(cert.getEncoded());
                log.info("Certificate added to Keychain");
            }
            catch (CertificateEncodingException e) {
                log.error(e.getMessage());
            }
            finally {
                NSAutoreleasePool.pop(pool);
            }
        }
    }
}
