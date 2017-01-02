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
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SSHFingerprintGenerator;
import ch.cyberduck.core.sftp.openssh.OpenSSHHostKeyVerifier;

import org.apache.log4j.Logger;

import java.security.PublicKey;
import java.text.MessageFormat;

import net.schmizz.sshj.common.KeyType;

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
        final AlertController alert = new AlertController() {
            @Override
            public void loadBundle() {
                final NSAlert alert = NSAlert.alert();
                alert.setMessageText(MessageFormat.format(LocaleFactory.localizedString("Unknown fingerprint", "Sftp"), hostname));
                alert.setInformativeText(MessageFormat.format(LocaleFactory.localizedString("The fingerprint for the {1} key sent by the server is {0}.", "Sftp"),
                        fingerprint, KeyType.fromKey(key).name()));
                alert.addButtonWithTitle(LocaleFactory.localizedString("Allow"));
                alert.addButtonWithTitle(LocaleFactory.localizedString("Deny"));
                alert.setShowsSuppressionButton(true);
                alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
                super.loadBundle(alert);
            }

            @Override
            protected String help() {
                return new DefaultProviderHelpService().help(Scheme.sftp);
            }
        };
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
        final AlertController alert = new AlertController() {
            @Override
            public void loadBundle() {
                final NSAlert alert = NSAlert.alert();
                alert.setMessageText(MessageFormat.format(LocaleFactory.localizedString("Changed fingerprint", "Sftp"), hostname));
                alert.setInformativeText(MessageFormat.format(LocaleFactory.localizedString("The fingerprint for the {1} key sent by the server is {0}.", "Sftp"),
                        fingerprint, KeyType.fromKey(key).name()));
                alert.addButtonWithTitle(LocaleFactory.localizedString("Allow"));
                alert.addButtonWithTitle(LocaleFactory.localizedString("Deny"));
                alert.setShowsSuppressionButton(true);
                alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
                super.loadBundle(alert);
            }

            @Override
            protected String help() {
                return new DefaultProviderHelpService().help(Scheme.sftp);
            }
        };
        switch(alert.beginSheet(controller)) {
            case SheetCallback.DEFAULT_OPTION:
                allow(hostname, key, alert.isSuppressed());
                return true;
        }
        log.warn("Cannot continue without a valid host key");
        throw new ConnectionCanceledException();
    }
}
