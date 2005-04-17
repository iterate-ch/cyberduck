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
import com.apple.cocoa.foundation.*;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.List;
import java.util.Vector;
import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;

import ch.cyberduck.core.ftps.AbstractX509TrustManager;
import ch.cyberduck.core.Keychain;

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
        this.alwaysButton.setEnabled(false);
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

    private boolean allowServerCertificate = false;
    private boolean allowClientCertificate = false;

    private List acceptedCertificates = new Vector();

    private KeyStore keystore = null;

    public CDX509TrustManagerController(CDWindowController windowController) {
        this.windowController = windowController;
        instances.addObject(this);
        if(false == NSApplication.loadNibNamed("Certificate", this)) {
            log.fatal("Couldn't load Certificate.nib");
        }
        try {
            this.init(keystore = KeyStore.getInstance(KeyStore.getDefaultType()));
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
        for(int i = 0; i < x509Certificates.length; i++) {
            try {
                if(!this.acceptedCertificates.contains(x509Certificates[i])) {
                    super.checkClientTrusted(x509Certificates, authType);
                }
                this.allowClientCertificate = true;
            }
            catch (CertificateException e) {
                String cert = "";
                if (x509Certificates.length > 0)
                    cert = x509Certificates[0].toString();
                alertField.setStringValue(NSBundle.localizedString(
                        "There is a problem with the client certificate. Reason: ", "")
                        + e.getMessage());
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
            if(alwaysButton.state() == NSCell.OnState) {
                this.saveToKeychain((X509Certificate) contextInfo);
            }
        }
        if (returncode == NSAlertPanel.AlternateReturn) {
            this.allowClientCertificate = false;
        }
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType)
            throws CertificateException {
        for(int i = 0; i < x509Certificates.length; i++) {
            try {
                if(!this.acceptedCertificates.contains(x509Certificates[i])) {
                    super.checkServerTrusted(x509Certificates, authType);
                }
                this.allowServerCertificate = true;
            }
            catch (CertificateException e) {
                if(x509Certificates[i].equals(
                        this.getFromKeychain(x509Certificates[i]))) {
                    log.info("Certificate accepted because it was found in the keychain.");
                    return;
                }
                String cert = "";
                if (x509Certificates.length > 0)
                    cert = x509Certificates[0].toString();
                alertField.setStringValue(NSBundle.localizedString(
                        "There is a problem with the client certificate. Reason: ", "")
                        + e.getMessage());
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
            if(alwaysButton.state() == NSCell.OnState) {
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

    public X509Certificate getFromKeychain(X509Certificate certificate) {
        try {
            int pool = NSAutoreleasePool.push();
            byte[] certData = Keychain.instance().getCertificateFromKeychain(certificate.getEncoded());
            NSAutoreleasePool.pop(pool);
            if(certData != null) {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                return (X509Certificate)factory.generateCertificate(new ByteArrayInputStream(certData));
            }
			log.info("Certificate not found in Keychain");
        }
        catch(CertificateException e) {
            log.error("Error getting certificate from the keychain: "+e.getMessage());
        }
        return null;
    }

    private void saveToKeychain(X509Certificate cert) {
            this.acceptedCertificates.add(cert);
        if(alwaysButton.state() == NSCell.OnState) {
            try {
                int pool = NSAutoreleasePool.push();
                Keychain.instance().addCertificateToKeychain(cert.getEncoded());
                NSAutoreleasePool.pop(pool);
            }
            catch (CertificateEncodingException e) {
                log.error(e.getMessage());
            }
        }
    }
}
