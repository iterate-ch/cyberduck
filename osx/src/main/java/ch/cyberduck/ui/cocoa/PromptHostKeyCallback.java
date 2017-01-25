package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SSHFingerprintGenerator;
import ch.cyberduck.core.sftp.openssh.OpenSSHHostKeyVerifier;
import ch.cyberduck.ui.cocoa.controller.ChangedHostKeyAlertController;
import ch.cyberduck.ui.cocoa.controller.UnknownHostKeyAlertController;

import org.apache.log4j.Logger;

import java.security.PublicKey;

/**
 * Using known_hosts from OpenSSH to store accepted host keys.
 */
public class PromptHostKeyCallback extends OpenSSHHostKeyVerifier {
    private static final Logger log = Logger.getLogger(PromptHostKeyCallback.class);

    private final WindowController controller;

    public PromptHostKeyCallback(final WindowController c) {
        this(c, LocalFactory.get(PreferencesFactory.get().getProperty("ssh.knownhosts")).withBookmark(
                PreferencesFactory.get().getProperty("ssh.knownhosts.bookmark")
        ));
    }

    public PromptHostKeyCallback(final WindowController controller, final Local file) {
        super(file);
        this.controller = controller;
    }

    @Override
    protected boolean isUnknownKeyAccepted(final String hostname, final PublicKey key)
            throws ConnectionCanceledException, ChecksumException {
        final String fingerprint = new SSHFingerprintGenerator().fingerprint(key);
        final AlertController alert = new UnknownHostKeyAlertController(hostname, fingerprint, key);
        switch(alert.beginSheet(controller)) {
            case SheetCallback.DEFAULT_OPTION:
                this.allow(hostname, key, alert.isSuppressed());
                return true;
        }
        log.warn("Cannot continue without a valid host key");
        throw new ConnectionCanceledException();
    }

    @Override
    protected boolean isChangedKeyAccepted(final String hostname, final PublicKey key)
            throws ConnectionCanceledException, ChecksumException {
        final String fingerprint = new SSHFingerprintGenerator().fingerprint(key);
        final AlertController alert = new ChangedHostKeyAlertController(hostname, fingerprint, key);
        switch(alert.beginSheet(controller)) {
            case SheetCallback.DEFAULT_OPTION:
                this.allow(hostname, key, alert.isSuppressed());
                return true;
        }
        log.warn("Cannot continue without a valid host key");
        throw new ConnectionCanceledException();
    }
}
