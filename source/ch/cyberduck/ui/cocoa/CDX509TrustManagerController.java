package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.sshtools.j2ssh.transport.InvalidHostFileException;

import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSSelector;

import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import org.apache.log4j.Logger;

import ch.cyberduck.core.ftps.AbstractX509TrustManager;

/**
 * @version $Id$
 */
public class CDX509TrustManagerController extends AbstractX509TrustManager {
    protected static Logger log = Logger.getLogger(CDX509TrustManagerController.class);

    private CDController windowController;

    private boolean allowServerCertificate = false;
    private boolean allowClientCertificate = false;

    public CDX509TrustManagerController(CDController windowController, KeyStore keystore) {
        this.windowController = windowController;
        try {
            this.init(keystore);
        }
        catch(NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        catch(KeyStoreException e) {
            log.error(e.getMessage());
        }
    }

    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
        try {
            super.checkClientTrusted(x509Certificates, authType);
            this.allowClientCertificate = true;
        }
        catch(CertificateException e) {
            String cert = "";
            if(x509Certificates.length > 0)
                cert = x509Certificates[0].toString();
            NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Certificate", ""), //title
                    NSBundle.localizedString("There is a problem with the client certificate. Reason:", "")+e.getMessage()
                    +"\n"+cert,
                    NSBundle.localizedString("Disconnect", ""), // defaultbutton
                    NSBundle.localizedString("Continue", ""),//alternate button
                    null); // other button
            this.windowController.beginSheet(sheet,
                    this, //delegate
                    new NSSelector
                            ("clientCertificateAlertSheetDidClose",
                                    new Class[]
                                    {
                                        NSWindow.class, int.class, Object.class
                                    }), // end selector
                    null);
            this.windowController.waitForSheetEnd();
            if(!allowClientCertificate) {
                throw e;
            }
        }
    }

    public void clientCertificateAlertSheetDidClose(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        if(returncode == NSAlertPanel.DefaultReturn) {
            this.allowClientCertificate = false;
        }
        if(returncode == NSAlertPanel.AlternateReturn) {
            this.allowClientCertificate = true;
        }
        synchronized(this.windowController) {
            this.windowController.notifyAll();
        }
    }


    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException{
        try {
            super.checkServerTrusted(x509Certificates, authType);
            this.allowServerCertificate = true;
        }
        catch(CertificateException e) {
            String cert = "";
            if(x509Certificates.length > 0)
                cert = x509Certificates[0].toString();
            NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Certificate", ""), //title
                    NSBundle.localizedString("There is a problem with the client certificate. Reason:", "")+e.getMessage()
                    +"\n"+cert,
                    NSBundle.localizedString("Disconnect", ""), // defaultbutton
                    NSBundle.localizedString("Continue", ""),//alternate button
                    null); // other button
            this.windowController.beginSheet(sheet,
                    this, //delegate
                    new NSSelector
                            ("serverCertificateAlertSheetDidClose",
                                    new Class[]
                                    {
                                        NSWindow.class, int.class, Object.class
                                    }), // end selector
                    null);
            this.windowController.waitForSheetEnd();
            if(!allowServerCertificate) {
                throw e;
            }
        }
    }

    public void serverCertificateAlertSheetDidClose(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        if(returncode == NSAlertPanel.DefaultReturn) {
            this.allowServerCertificate = false;
        }
        if(returncode == NSAlertPanel.AlternateReturn) {
            this.allowServerCertificate = true;
        }
        synchronized(this.windowController) {
            this.windowController.notifyAll();
        }
    }

}
