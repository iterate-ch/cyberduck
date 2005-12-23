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

import com.sshtools.j2ssh.transport.AbstractKnownHostsKeyVerification;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import ch.cyberduck.core.Preferences;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 *          Concrete Coccoa implementation of a SSH HostKeyVerification
 */
public class CDHostKeyController extends CDSheetController implements HostKeyVerification {

    private static Logger log = Logger.getLogger(CDHostKeyController.class);

    private SshPublicKey publicKey;

    private AbstractKnownHostsKeyVerification delegate;

    public CDHostKeyController(CDWindowController parent) {
        super(parent);
        try {
            this.delegate = new AbstractKnownHostsKeyVerification() {
                public void onHostKeyMismatch(String host, SshPublicKey allowedHostKey, SshPublicKey actualHostKey) {
                    CDHostKeyController.this.onHostKeyMismatch(host, allowedHostKey, actualHostKey);
                }

                public void onUnknownHost(String host, SshPublicKey key) {
                    CDHostKeyController.this.onUnknownHost(host, key);
                }
            };
            this.delegate.setKnownHostFile(Preferences.instance().getProperty("ssh.knownhosts"));
        }
        catch (com.sshtools.j2ssh.transport.InvalidHostFileException e) {
            //This exception is thrown whenever an exception occurs open or reading from the host file.
            this.window = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Error", ""), //title
                    NSBundle.localizedString("Could not open or read the host file", "") + ": " + e.getMessage(), // message
                    NSBundle.localizedString("OK", ""), // defaultbutton
                    null, //alternative button
                    null //other button
            );
            this.beginSheet();
        }
    }

    public void onHostKeyMismatch(final String host, final SshPublicKey allowedHostKey, final SshPublicKey actualHostKey) {
        log.debug("onHostKeyMismatch");
        this.publicKey = actualHostKey;
        this.window = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Host key mismatch:", "") + " " + host, //title
                NSBundle.localizedString("The host key supplied is", "") + ": "
                        + actualHostKey.getFingerprint() +
                        "\n" + NSBundle.localizedString("The current allowed key for this host is", "") + " : "
                        + allowedHostKey.getFingerprint() + "\n" + NSBundle.localizedString("Do you want to allow the host access?", ""),
                NSBundle.localizedString("Allow", ""), // defaultbutton
                NSBundle.localizedString("Deny", ""), //alternative button
                delegate.isHostFileWriteable() ? NSBundle.localizedString("Always", "") : null //other button
        );
        this.beginSheet(null, new CDSheetListener() {
            public void dismissedSheet(int returncode, Object context) {
                try {
                    if (returncode == NSAlertPanel.DefaultReturn) {
                        delegate.allowHost(host, publicKey, false);
                    }
                    if (returncode == NSAlertPanel.AlternateReturn) {
                        log.info("Cannot continue without a valid host key");
                    }
                    if (returncode == NSAlertPanel.OtherReturn) {
                        delegate.allowHost(host, publicKey, true); // always allow host
                    }
                }
                catch (InvalidHostFileException e) {
                    log.error(e.getMessage());
                }
            }
        });
        this.waitForSheetEnd();
    }

    public void onUnknownHost(final String host,
                              final SshPublicKey publicKey) {
        log.debug("onUnknownHost");
        this.publicKey = publicKey;
        this.window = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Unknown host key for", "") + " " + host, //title
                NSBundle.localizedString("The host is currently unknown to the system. The host key fingerprint is", "") + ": " + publicKey.getFingerprint() + ".",
                NSBundle.localizedString("Allow", ""), // defaultbutton
                NSBundle.localizedString("Deny", ""), //alternative button
                delegate.isHostFileWriteable() ? NSBundle.localizedString("Always", "") : null //other button
        );
        this.beginSheet(null, new CDSheetListener() {
            public void dismissedSheet(int returncode, Object context) {
                try {
                    if (returncode == NSAlertPanel.DefaultReturn) {
                        delegate.allowHost(host, publicKey, false); // allow host
                    }
                    if (returncode == NSAlertPanel.AlternateReturn) {
                        log.info("Cannot continue without a valid host key");
                    }
                    if (returncode == NSAlertPanel.OtherReturn) {
                        delegate.allowHost(host, publicKey, true); // always allow host
                    }
                }
                catch (InvalidHostFileException e) {
                    log.error(e.getMessage());
                }
            }
        });
        this.waitForSheetEnd();
    }

    protected void invalidate() {
        ;
    }

    public boolean verifyHost(String host, SshPublicKey pk) throws TransportProtocolException {
        return delegate.verifyHost(host, pk);
    }
}