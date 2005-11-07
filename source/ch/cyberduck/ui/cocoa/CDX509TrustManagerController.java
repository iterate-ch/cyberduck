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
import ch.cyberduck.core.ftps.AbstractX509TrustManager;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSCell;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSTextView;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.foundation.NSAutoreleasePool;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public class CDX509TrustManagerController extends AbstractX509TrustManager {
    protected static Logger log = Logger.getLogger(CDX509TrustManagerController.class);

    private static NSMutableArray instances = new NSMutableArray();

    public void awakeFromNib() {
        this.window().setReleasedWhenClosed(false);
    }

    public void windowWillClose(NSNotification notification) {
        instances.removeObject(this);
    }

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

    private NSWindow sheet;

    public void setWindow(NSWindow window) {
        this.sheet = window;
        this.sheet.setDelegate(this);
    }

    public NSWindow window() {
        return this.sheet;
    }

    private CDWindowController windowController;

    public CDX509TrustManagerController(CDWindowController windowController) {
        this.windowController = windowController;
        instances.addObject(this);
        if (!NSApplication.loadNibNamed("Certificate", this)) {
            log.fatal("Couldn't load Certificate.nib");
        }
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

    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {
        for (int i = 0; i < x509Certificates.length; i++) {
            try {
                if (!this.acceptedCertificates.contains(x509Certificates[i])) {
                    super.checkClientTrusted(x509Certificates, authType);
                }
                this.allowClientCertificate = true;
            }
            catch (CertificateException e) {
                if (this.keychainKnowsAbout(x509Certificates[i])) {
                    return;
                }
                String cert = "";
                if (x509Certificates.length > 0)
                    cert = x509Certificates[0].toString();
                alertField.setStringValue(e.getMessage());
                certificateField.setString(cert);
                this.windowController.beginSheet(this.sheet,
                        this, //delegate
                        new NSSelector
                                ("clientCertificateAlertSheetDidEnd",
                                        new Class[]
                                                {
                                                        NSWindow.class, int.class, Object.class
                                                }), // end selector
                        x509Certificates[0]);
                this.windowController.waitForSheetEnd();
                if (!allowClientCertificate) {
                    throw e;
                }
            }
        }
    }

    public void clientCertificateAlertSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.allowClientCertificate = true;
            if (alwaysButton.state() == NSCell.OnState) {
                this.saveToKeychain((X509Certificate) contextInfo);
            }
        }
        if (returncode == NSAlertPanel.AlternateReturn) {
            this.allowClientCertificate = false;
        }
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {
        for (int i = 0; i < x509Certificates.length; i++) {
            try {
                if (!this.acceptedCertificates.contains(x509Certificates[i])) {
                    super.checkServerTrusted(x509Certificates, authType);
                }
                this.allowServerCertificate = true;
            }
            catch (CertificateException e) {
                if (this.keychainKnowsAbout(x509Certificates[i])) {
                    return;
                }
                String cert = "";
                if (x509Certificates.length > 0)
                    cert = x509Certificates[0].toString();
                alertField.setStringValue(e.getMessage());
                certificateField.setString(cert);
                this.windowController.beginSheet(sheet,
                        this, //delegate
                        new NSSelector
                                ("serverCertificateAlertSheetDidEnd",
                                        new Class[]
                                                {
                                                        NSWindow.class, int.class, Object.class
                                                }), // end selector
                        x509Certificates[0]);
                this.windowController.waitForSheetEnd();
                if (!allowServerCertificate) {
                    throw e;
                }
            }
        }
    }

    public void serverCertificateAlertSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        if (returncode == NSAlertPanel.DefaultReturn) { //Allow
            this.allowServerCertificate = true;
            this.acceptedCertificates.add(contextInfo);
            if (alwaysButton.state() == NSCell.OnState) {
                this.saveToKeychain((X509Certificate) contextInfo);
            }
        }
        if (returncode == NSAlertPanel.AlternateReturn) { //Deny
            this.allowServerCertificate = false;
        }
    }

    public void closeSheet(NSButton sender) {
        this.windowController.endSheet(this.window(), sender.tag());
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
        log.info("Certificate not found in Keychain");
        return false;
    }

    private void saveToKeychain(X509Certificate cert) {
        this.acceptedCertificates.add(cert);
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
