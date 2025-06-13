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
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSProgressIndicator;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.StringAppender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.cocoa.foundation.NSRect;

public class ProgressAlertController extends AlertController {
    private static final Logger log = LogManager.getLogger(ProgressAlertController.class);

    private final String title;
    private final String message;
    private final Protocol protocol;

    public ProgressAlertController(final String title, final String message, final Protocol protocol) {
        this.title = title;
        this.message = message;
        this.protocol = protocol;
    }

    @Override
    public NSAlert loadAlert() {
        log.debug("Load alert for message {}", message);
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(title);
        alert.setInformativeText(new StringAppender().append(message).toString());
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel"));
        alert.setShowsHelp(true);
        alert.setShowsSuppressionButton(false);
        return alert;
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        final NSProgressIndicator progress = NSProgressIndicator.progressIndicatorWithFrame(new NSRect(0, 18));
        progress.setIndeterminate(true);
        progress.setDisplayedWhenStopped(false);
        progress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
        progress.setControlSize(NSCell.NSRegularControlSize);
        progress.startAnimation(this.id());
        return progress;
    }

    @Override
    protected String help() {
        return ProviderHelpServiceFactory.get().help(protocol);
    }
}
