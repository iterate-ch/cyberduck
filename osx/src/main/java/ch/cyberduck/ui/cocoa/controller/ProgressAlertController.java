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
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.SheetInvoker;
import ch.cyberduck.binding.WindowController;
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
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.concurrent.CountDownLatch;

public class ProgressAlertController extends AlertController {
    private static final Logger log = LogManager.getLogger(ProgressAlertController.class);

    private final CountDownLatch signal;
    private final String title;
    private final String message;
    private final Protocol protocol;

    @Outlet
    private NSView view;
    @Outlet
    private NSProgressIndicator progress;

    public ProgressAlertController(final CountDownLatch signal, final String title, final String message, final Protocol protocol) {
        this.signal = signal;
        this.title = title;
        this.message = message;
        this.protocol = protocol;
    }

    @Override
    public void loadBundle() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Load alert for message %s", message));
        }
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(title);
        alert.setInformativeText(new StringAppender().append(message).toString());
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel"));
        alert.setShowsHelp(true);
        alert.setShowsSuppressionButton(false);
        super.loadBundle(alert);
    }

    public int beginSheet(final WindowController parent) {
        return new SheetInvoker(this, parent, this, signal).beginSheet();
    }

    @Override
    public NSView getAccessoryView(final NSAlert alert) {
        view = NSView.create(new NSRect(alert.window().frame().size.width.doubleValue(), 0));
        progress = NSProgressIndicator.progressIndicatorWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 18));
        progress.setIndeterminate(true);
        progress.setDisplayedWhenStopped(false);
        progress.setStyle(NSProgressIndicator.NSProgressIndicatorSpinningStyle);
        progress.setControlSize(NSCell.NSRegularControlSize);
        progress.setFrameOrigin(new NSPoint(0, 0));
        view.addSubview(progress);
        progress.startAnimation(this.id());
        return view;
    }

    @Override
    protected String help() {
        return ProviderHelpServiceFactory.get().help(protocol);
    }
}
