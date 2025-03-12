package ch.cyberduck.ui.cocoa.callback;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SSHFingerprintGenerator;
import ch.cyberduck.core.sftp.openssh.OpenSSHHostKeyVerifier;
import ch.cyberduck.ui.cocoa.controller.ChangedHostKeyAlertController;
import ch.cyberduck.ui.cocoa.controller.UnknownHostKeyAlertController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.PublicKey;

/**
 * Using known_hosts from OpenSSH to store accepted host keys.
 */
public class PromptHostKeyCallback extends OpenSSHHostKeyVerifier {
    private static final Logger log = LogManager.getLogger(PromptHostKeyCallback.class);

    private final Preferences preferences = PreferencesFactory.get();
    private final WindowController controller;
    private final Local file;

    public PromptHostKeyCallback(final WindowController c) {
        this(c, LocalFactory.get(PreferencesFactory.get().getProperty("ssh.knownhosts")).setBookmark(
                PreferencesFactory.get().getProperty("ssh.knownhosts.bookmark")
        ));
    }

    public PromptHostKeyCallback(final WindowController controller, final Local file) {
        super(file);
        this.controller = controller;
        this.file = file;
    }

    @Override
    protected boolean isUnknownKeyAccepted(final Host hostname, final PublicKey key) throws BackgroundException {
        final String fingerprint = new SSHFingerprintGenerator().fingerprint(key);
        final AlertController alert = new UnknownHostKeyAlertController(hostname.getHostname(), fingerprint, key);
        switch(alert.beginSheet(controller)) {
            case SheetCallback.DEFAULT_OPTION:
                final Object lock = file.lock(true);
                try {
                    this.allow(hostname, key, alert.isSuppressed());
                }
                finally {
                    file.release(lock);
                    preferences.setProperty("ssh.knownhosts.bookmark", file.getBookmark());
                }
                return true;
        }
        log.warn("Cannot continue without a valid host key");
        throw new ConnectionCanceledException();
    }

    @Override
    protected boolean isChangedKeyAccepted(final Host hostname, final PublicKey key) throws BackgroundException {
        final String fingerprint = new SSHFingerprintGenerator().fingerprint(key);
        final AlertController alert = new ChangedHostKeyAlertController(hostname.getHostname(), fingerprint, key);
        switch(alert.beginSheet(controller)) {
            case SheetCallback.DEFAULT_OPTION:
                final Object lock = file.lock(true);
                try {
                    this.allow(hostname, key, alert.isSuppressed());
                }
                finally {
                    file.release(lock);
                    preferences.setProperty("ssh.knownhosts.bookmark", file.getBookmark());
                }
                return true;
        }
        log.warn("Cannot continue without a valid host key");
        throw new ConnectionCanceledException();
    }
}
