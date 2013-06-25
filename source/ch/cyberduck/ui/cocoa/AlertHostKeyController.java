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
import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.HostKeyControllerFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.sftp.MemoryHostKeyVerifier;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSCell;
import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import ch.ethz.ssh2.KnownHosts;

/**
 * Using known_hosts from OpenSSH to store accepted host keys.
 *
 * @version $Id$
 */
public class AlertHostKeyController extends MemoryHostKeyVerifier {
    private static final Logger log = Logger.getLogger(AlertHostKeyController.class);

    public static void register() {
        HostKeyControllerFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends HostKeyControllerFactory {
        @Override
        protected HostKeyController create() {
            return null;
        }

        @Override
        public HostKeyController create(Controller c) {
            return new AlertHostKeyController((WindowController) c);
        }
    }

    private WindowController parent;

    /**
     * Path to known_hosts file.
     */
    private final Local file;

    public AlertHostKeyController(final WindowController c) {
        this(c, LocalFactory.createLocal(Preferences.instance().getProperty("ssh.knownhosts")));
    }

    public AlertHostKeyController(final WindowController parent, final Local file) {
        super(file);
        this.file = file;
        this.parent = parent;
    }

    @Override
    protected boolean isHostKeyDatabaseWritable() {
        return file.attributes().getPermission().isWritable();
    }

    @Override
    protected boolean isUnknownKeyAccepted(final String hostname, final int port, final String serverHostKeyAlgorithm,
                                           final byte[] serverHostKey) throws ConnectionCanceledException {
        final NSAlert alert = NSAlert.alert(MessageFormat.format(Locale.localizedString("Unknown host key for {0}."), hostname), //title
                MessageFormat.format(Locale.localizedString("The host is currently unknown to the system. The host key fingerprint is {0}."),
                        KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)),
                Locale.localizedString("Allow"), // default button
                Locale.localizedString("Deny"), // alternate button
                null //other button
        );
        if(this.isHostKeyDatabaseWritable()) {
            alert.setShowsSuppressionButton(true);
            alert.suppressionButton().setTitle(Locale.localizedString("Always"));
        }
        alert.setShowsHelp(true);
        SheetController c = new AlertController(parent, alert) {
            @Override
            public void callback(final int returncode) {
                if(returncode == DEFAULT_OPTION) {// allow host (once)
                    allow(hostname, serverHostKeyAlgorithm, serverHostKey,
                            alert.suppressionButton().state() == NSCell.NSOnState);
                }
                else {
                    log.warn("Cannot continue without a valid host key");
                }
            }

            @Override
            protected void help() {
                StringBuilder site = new StringBuilder(Preferences.instance().getProperty("website.help"));
                site.append("/").append(Protocol.SFTP.getIdentifier());
                openUrl(site.toString());
            }
        };
        c.beginSheet();
        if(c.returnCode() == SheetCallback.ALTERNATE_OPTION) {
            throw new ConnectionCanceledException();
        }
        return c.returnCode() == SheetCallback.DEFAULT_OPTION;

    }

    @Override
    protected boolean isChangedKeyAccepted(final String hostname, final int port, final String serverHostKeyAlgorithm,
                                           final byte[] serverHostKey) throws ConnectionCanceledException {
        NSAlert alert = NSAlert.alert(MessageFormat.format(Locale.localizedString("Host key mismatch for {0}"), hostname), //title
                MessageFormat.format(Locale.localizedString("The host key supplied is {0}."),
                        KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)),
                Locale.localizedString("Allow"), // defaultbutton
                Locale.localizedString("Deny"), //alternative button
                null //other button
        );
        if(this.isHostKeyDatabaseWritable()) {
            alert.setShowsSuppressionButton(true);
            alert.suppressionButton().setTitle(Locale.localizedString("Always"));
        }
        alert.setShowsHelp(true);
        SheetController c = new AlertController(parent, alert) {
            @Override
            public void callback(final int returncode) {
                if(returncode == DEFAULT_OPTION) {
                    allow(hostname, serverHostKeyAlgorithm, serverHostKey,
                            alert.suppressionButton().state() == NSCell.NSOnState);
                }
                else {
                    log.warn("Cannot continue without a valid host key");
                }
            }

            @Override
            protected void help() {
                StringBuilder site = new StringBuilder(Preferences.instance().getProperty("website.help"));
                site.append("/").append(Protocol.SFTP.getIdentifier());
                openUrl(site.toString());
            }
        };
        c.beginSheet();
        if(c.returnCode() == SheetCallback.ALTERNATE_OPTION) {
            throw new ConnectionCanceledException();
        }
        return c.returnCode() == SheetCallback.DEFAULT_OPTION;

    }

    @Override
    public boolean verify(final String hostname, final int port, final String serverHostKeyAlgorithm,
                          final byte[] serverHostKey) throws IOException, ConnectionCanceledException {
        final NSAutoreleasePool pool = NSAutoreleasePool.push();
        try {
            return super.verify(hostname, port, serverHostKeyAlgorithm, serverHostKey);
        }
        finally {
            pool.drain();
        }
    }

    @Override
    protected void save(final String hostname,
                        final String serverHostKeyAlgorithm, final byte[] serverHostKey) throws IOException {
        // Also try to add the key to a known_host file
        KnownHosts.addHostkeyToFile(new File(file.getAbsolute()),
                new String[]{KnownHosts.createHashedHostname(hostname)},
                serverHostKeyAlgorithm, serverHostKey);
    }
}
