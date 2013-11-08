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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.sftp.MemoryHostKeyVerifier;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.HostKeyControllerFactory;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSCell;
import ch.cyberduck.ui.cocoa.application.NSOpenPanel;
import ch.cyberduck.ui.cocoa.application.NSWindow;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;

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
            throw new FactoryException();
        }

        @Override
        public HostKeyController create(final Controller c, final Protocol protocol) {
            if(Scheme.sftp.equals(protocol.getScheme())) {
                return new AlertHostKeyController((WindowController) c);
            }
            return new DefaultHostKeyController();
        }
    }

    private WindowController parent;

    /**
     * Path to known_hosts file.
     */
    private final Local file;

    private NSOpenPanel panel;

    public AlertHostKeyController(final WindowController c) {
        this(c, LocalFactory.createLocal(Preferences.instance().getProperty("ssh.knownhosts")).withBookmark(
                Preferences.instance().getProperty("ssh.knownhosts.bookmark")
        ));
    }

    public AlertHostKeyController(final WindowController parent, final Local file) {
        super(file);
        this.file = file;
        this.parent = parent;
    }

    @Override
    protected boolean isHostKeyDatabaseWritable() {
        if(!file.attributes().getPermission().isWritable()) {
            // Ask for known_hosts file to select. Possibly not allowed by sandbox
            final SheetController sheet = new SheetController(parent) {
                @Override
                public void callback(int returncode) {
                    //
                }

                @Override
                protected void beginSheetImpl() {
                    panel = NSOpenPanel.openPanel();
                    panel.setCanChooseDirectories(false);
                    panel.setCanChooseFiles(true);
                    panel.setAllowsMultipleSelection(false);
                    panel.setMessage(LocaleFactory.localizedString("Select the SSH known_hosts file to save host keys.", "Credentials"));
                    panel.setPrompt(LocaleFactory.localizedString("Choose"));
                    panel.beginSheetForDirectory(LocalFactory.createLocal("~/.ssh").getAbsolute(),
                            null, parent.window(), this.id(), Foundation.selector("sheetDidClose:returnCode:contextInfo:"), null);
                }

                @Override
                public NSWindow window() {
                    return panel;
                }
            };
            sheet.beginSheet();
            if(sheet.returnCode() == SheetCallback.DEFAULT_OPTION) {
                final NSObject selected = panel.filenames().lastObject();
                if(selected != null) {
                    final Local f = LocalFactory.createLocal(selected.toString());
                    Preferences.instance().setProperty("ssh.knownhosts", f.getAbbreviatedPath());
                    Preferences.instance().setProperty("ssh.knownhosts.bookmark", f.getBookmark());
                    setDatabase(f);
                    return f.attributes().getPermission().isWritable();
                }
            }
            if(sheet.returnCode() == SheetCallback.CANCEL_OPTION) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean isUnknownKeyAccepted(final String hostname, final int port, final String serverHostKeyAlgorithm,
                                           final byte[] serverHostKey) throws ConnectionCanceledException {
        final NSAlert alert = NSAlert.alert(MessageFormat.format(LocaleFactory.localizedString("Unknown host key for {0}."), hostname), //title
                MessageFormat.format(LocaleFactory.localizedString("The host is currently unknown to the system. The host key fingerprint is {0}."),
                        KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)),
                LocaleFactory.localizedString("Allow"), // default button
                LocaleFactory.localizedString("Deny"), // alternate button
                null //other button
        );
        if(this.isHostKeyDatabaseWritable()) {
            alert.setShowsSuppressionButton(true);
            alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
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
                new DefaultProviderHelpService().help(Scheme.sftp);
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
        NSAlert alert = NSAlert.alert(MessageFormat.format(LocaleFactory.localizedString("Host key mismatch for {0}"), hostname), //title
                MessageFormat.format(LocaleFactory.localizedString("The host key supplied is {0}."),
                        KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)),
                LocaleFactory.localizedString("Allow"), // defaultbutton
                LocaleFactory.localizedString("Deny"), //alternative button
                null //other button
        );
        if(this.isHostKeyDatabaseWritable()) {
            alert.setShowsSuppressionButton(true);
            alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
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
                new DefaultProviderHelpService().help(Scheme.sftp);
            }
        };
        c.beginSheet();
        if(c.returnCode() == SheetCallback.ALTERNATE_OPTION) {
            throw new ConnectionCanceledException();
        }
        return c.returnCode() == SheetCallback.DEFAULT_OPTION;

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
