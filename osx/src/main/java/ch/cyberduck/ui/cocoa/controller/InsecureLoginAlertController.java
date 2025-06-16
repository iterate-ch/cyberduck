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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.StringAppender;

public class InsecureLoginAlertController extends AlertController {

    private final String title;
    private final String message;
    private final String continueButton;
    private final String disconnectButton;
    private final Protocol protocol;
    private final boolean enableSuppressionButton;

    public InsecureLoginAlertController(final String title, final String message,
                                        final String continueButton, final String disconnectButton,
                                        final Protocol protocol, final boolean enableSuppressionButton) {
        this.title = title;
        this.message = message;
        this.continueButton = continueButton;
        this.disconnectButton = disconnectButton;
        this.protocol = protocol;
        this.enableSuppressionButton = enableSuppressionButton;
    }

    @Override
    public NSAlert loadAlert() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
        alert.setMessageText(title);
        alert.setInformativeText(new StringAppender().append(message).toString());
        alert.addButtonWithTitle(continueButton);
        alert.addButtonWithTitle(disconnectButton);
        alert.setShowsHelp(true);
        alert.setShowsSuppressionButton(enableSuppressionButton);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't show again", "Credentials"));
        return alert;
    }

    @Override
    protected String help() {
        return ProviderHelpServiceFactory.get().help(protocol);
    }
}
