package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.ServerHostKeyVerifier;

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Local;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @version $Id$
 */
public class CDHostKeyController implements ServerHostKeyVerifier {
    protected static Logger log = Logger.getLogger(CDHostKeyController.class);

    private CDWindowController parent;

    /**
     * It is a thread safe implementation, therefore, you need only to instantiate one
     * <code>KnownHosts</code> for your whole application.
     */
    private KnownHosts database;

    public CDHostKeyController(final CDWindowController windowController) {
        this.parent = windowController;
        Local f = new Local(Preferences.instance().getProperty("ssh.knownhosts"));
        if(f.isReadable()) {
            try {
                this.database = new KnownHosts(f.getAbsolute());
            }
            catch(IOException e) {
                log.error("Cannot read "+f.getAbsolute());
            }
        }
        if(null == this.database) {
            this.database = new KnownHosts();
        }
    }

    public boolean verifyServerHostKey(final String hostname, final int port, final String serverHostKeyAlgorithm,
                                       final byte[] serverHostKey) throws Exception {
        int result = database.verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);
        if(KnownHosts.HOSTKEY_IS_OK == result) {
            return true; // We are happy
        }
        if(KnownHosts.HOSTKEY_IS_NEW == result) {
            NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Unknown host key for", "") + " "
                    + hostname, //title
                    NSBundle.localizedString("The host is currently unknown to the system. The host key fingerprint is", "")
                            + ": " + KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey) + ".",
                    NSBundle.localizedString("Allow", ""), // default button
                    NSBundle.localizedString("Deny", ""), // alternate button
                    new Local(Preferences.instance().getProperty("ssh.knownhosts")).isWritable() ?
                            NSBundle.localizedString("Always", "") : null //other button
            );
            CDSheetController c = new CDSheetController(parent, sheet) {
                public void callback(final int returncode) {
                    if(returncode == DEFAULT_OPTION) {// allow host (once)
                        allow(hostname, serverHostKeyAlgorithm, serverHostKey, false);
                    }
                    if(returncode == ALTERNATE_OPTION) {// allow host (always)
                        allow(hostname, serverHostKeyAlgorithm, serverHostKey, true);
                    }
                    if(returncode == CANCEL_OPTION) {
                        log.warn("Cannot continue without a valid host key");
                    }
                }
            };
            c.beginSheet(true);
            return c.returnCode() == CDSheetCallback.DEFAULT_OPTION
                    || c.returnCode() == CDSheetCallback.ALTERNATE_OPTION;
        }
        if(KnownHosts.HOSTKEY_HAS_CHANGED == result) {
            NSWindow sheet = NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Host key mismatch:", "") + " " + hostname, //title
                    NSBundle.localizedString("The host key supplied is", "") + ": "
                            + KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)
//                            + "\n" + NSBundle.localizedString("The current allowed key for this host is", "") + " : "
//                            + allowedHostKey.getFingerprint() + "\n"
                            + NSBundle.localizedString("Do you want to allow the host access?", ""),
                    NSBundle.localizedString("Allow", ""), // defaultbutton
                    NSBundle.localizedString("Deny", ""), //alternative button
                    new Local(Preferences.instance().getProperty("ssh.knownhosts")).isWritable() ? NSBundle.localizedString("Always", "") : null //other button
            );
            CDSheetController c = new CDSheetController(parent, sheet) {
                public void callback(final int returncode) {
                    if(returncode == DEFAULT_OPTION) {
                        allow(hostname, serverHostKeyAlgorithm, serverHostKey, false);
                    }
                    if(returncode == ALTERNATE_OPTION) {
                        allow(hostname, serverHostKeyAlgorithm, serverHostKey, true);
                    }
                    if(returncode == CANCEL_OPTION) {
                        log.warn("Cannot continue without a valid host key");
                    }
                }
            };
            c.beginSheet(true);
            return c.returnCode() == CDSheetCallback.DEFAULT_OPTION
                    || c.returnCode() == CDSheetCallback.ALTERNATE_OPTION;
        }
        return false;
    }

    private void allow(final String hostname, final String serverHostKeyAlgorithm,
                                       final byte[] serverHostKey, boolean always) {
        // The following call will ONLY put the key into the memory cache!
        // To save it in a known hosts file, also call "KnownHosts.addHostkeyToFile(...)"
        String hashedHostname = KnownHosts.createHashedHostname(hostname);
        // Add the hostkey to the in-memory database
        try {
            database.addHostkey(new String[]{hashedHostname}, serverHostKeyAlgorithm, serverHostKey);
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        if(always) {
            // Also try to add the key to a known_host file
            try {
                KnownHosts.addHostkeyToFile(new File(Preferences.instance().getProperty("ssh.knownhosts")),
                        new String[]{KnownHosts.createHashedHostname(hostname)},
                        serverHostKeyAlgorithm, serverHostKey);
            }
            catch(IOException ignore) {
                log.error(ignore.getMessage());
            }
        }
    }
}
