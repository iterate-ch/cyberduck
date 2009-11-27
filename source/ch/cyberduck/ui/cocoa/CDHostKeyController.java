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

import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.ServerHostKeyVerifier;

/**
 * @version $Id$
 */
public class CDHostKeyController extends ProxyController implements ServerHostKeyVerifier {
    protected static Logger log = Logger.getLogger(CDHostKeyController.class);

    private CDWindowController parent;

    /**
     * It is a thread safe implementation, therefore, you need only to instantiate one
     * <code>KnownHosts</code> for your whole application.
     */
    private KnownHosts database;

    public CDHostKeyController(final CDWindowController windowController) {
        this.parent = windowController;
        Local f = LocalFactory.createLocal(Preferences.instance().getProperty("ssh.knownhosts"));
        if(!f.exists()) {
            f.getParent().mkdir(true);
            f.touch();
        }
        if(f.isReadable()) {
            try {
                this.database = new KnownHosts(f.getAbsolute());
            }
            catch(IOException e) {
                log.error("Cannot read " + f.getAbsolute());
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
        final NSAutoreleasePool pool = NSAutoreleasePool.push();
        try {
            if(KnownHosts.HOSTKEY_IS_NEW == result) {
                NSAlert alert = NSAlert.alert(Locale.localizedString("Unknown host key for") + " "
                        + hostname, //title
                        Locale.localizedString("The host is currently unknown to the system. The host key fingerprint is")
                                + ": " + KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey) + ".",
                        Locale.localizedString("Allow"), // default button
                        Locale.localizedString("Deny"), // alternate button
                        LocalFactory.createLocal(Preferences.instance().getProperty("ssh.knownhosts")).isWritable() ?
                                Locale.localizedString("Always") : null //other button
                );
                CDSheetController c = new CDAlertController(parent, alert) {
                    public void callback(final int returncode) {
                        if(returncode == DEFAULT_OPTION) {// allow host (once)
                            allow(hostname, serverHostKeyAlgorithm, serverHostKey, false);
                        }
                        if(returncode == OTHER_OPTION) {// allow host (always)
                            allow(hostname, serverHostKeyAlgorithm, serverHostKey, true);
                        }
                        if(returncode == ALTERNATE_OPTION) {
                            log.warn("Cannot continue without a valid host key");
                        }
                    }
                };
                c.beginSheet();
                if(c.returnCode() == CDSheetCallback.ALTERNATE_OPTION) {
                    throw new ConnectionCanceledException();
                }
                return c.returnCode() == CDSheetCallback.DEFAULT_OPTION
                        || c.returnCode() == CDSheetCallback.OTHER_OPTION;
            }
            if(KnownHosts.HOSTKEY_HAS_CHANGED == result) {
                NSAlert alert = NSAlert.alert(Locale.localizedString("Host key mismatch:") + " " + hostname, //title
                        Locale.localizedString("The host key supplied is") + ": "
                                + KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)
//                                + "\n" + Locale.localizedString("The current allowed key for this host is") + " : "
//                                + allowedHostKey.getFingerprint()
                                + "\n"
                                + Locale.localizedString("Do you want to allow the host access?"),
                        Locale.localizedString("Allow"), // defaultbutton
                        Locale.localizedString("Deny"), //alternative button
                        LocalFactory.createLocal(Preferences.instance().getProperty("ssh.knownhosts")).isWritable() ? Locale.localizedString("Always") : null //other button
                );
                CDSheetController c = new CDAlertController(parent, alert) {
                    public void callback(final int returncode) {
                        if(returncode == DEFAULT_OPTION) {
                            allow(hostname, serverHostKeyAlgorithm, serverHostKey, false);
                        }
                        if(returncode == OTHER_OPTION) {
                            allow(hostname, serverHostKeyAlgorithm, serverHostKey, true);
                        }
                        if(returncode == ALTERNATE_OPTION) {
                            log.warn("Cannot continue without a valid host key");
                        }
                    }
                };
                c.beginSheet();
                if(c.returnCode() == CDSheetCallback.ALTERNATE_OPTION) {
                    throw new ConnectionCanceledException();
                }
                return c.returnCode() == CDSheetCallback.DEFAULT_OPTION
                        || c.returnCode() == CDSheetCallback.OTHER_OPTION;
            }
            return false;
        }
        finally {
            pool.drain();
        }
    }

    private void allow(final String hostname, final String serverHostKeyAlgorithm,
                       final byte[] serverHostKey, boolean always) {
        // The following call will ONLY put the key into the memory cache!
        // To save it in a known hosts file, also call "KnownHosts.addHostkeyToFile(...)"
        String hashedHostname = KnownHosts.createHashedHostname(hostname);
        try {
            // Add the hostkey to the in-memory database
            database.addHostkey(new String[]{hashedHostname}, serverHostKeyAlgorithm, serverHostKey);
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        if(always) {
            // Also try to add the key to a known_host file
            try {
                KnownHosts.addHostkeyToFile(new File(LocalFactory.createLocal(Preferences.instance().getProperty("ssh.knownhosts")).getAbsolute()),
                        new String[]{KnownHosts.createHashedHostname(hostname)},
                        serverHostKeyAlgorithm, serverHostKey);
            }
            catch(IOException ignore) {
                log.error(ignore.getMessage());
            }
        }
    }
}
