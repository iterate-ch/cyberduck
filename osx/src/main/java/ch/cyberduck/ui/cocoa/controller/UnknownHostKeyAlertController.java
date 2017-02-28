package ch.cyberduck.ui.cocoa.controller;

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
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

import java.security.PublicKey;
import java.text.MessageFormat;

import net.schmizz.sshj.common.KeyType;

public class UnknownHostKeyAlertController extends AlertController {
    private final String hostname;
    private final String fingerprint;
    private final PublicKey key;

    public UnknownHostKeyAlertController(final String hostname, final String fingerprint, final PublicKey key) {
        this.hostname = hostname;
        this.fingerprint = fingerprint;
        this.key = key;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
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
}
