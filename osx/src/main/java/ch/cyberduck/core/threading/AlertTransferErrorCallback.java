package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.ui.cocoa.AlertController;
import ch.cyberduck.ui.cocoa.WindowController;

import java.util.concurrent.atomic.AtomicBoolean;

public class AlertTransferErrorCallback implements TransferErrorCallback {

    private final WindowController controller;

    private boolean suppressed;

    private boolean option;

    public AlertTransferErrorCallback(final WindowController controller) {
        this.controller = controller;
    }

    @Override
    public boolean prompt(final BackgroundException failure) {
        if(suppressed) {
            return option;
        }
        if(controller.isVisible()) {
            final AtomicBoolean c = new AtomicBoolean(true);
            final NSAlert alert = NSAlert.alert(
                    null == failure.getMessage() ? LocaleFactory.localizedString("Unknown") : failure.getMessage(),
                    null == failure.getDetail() ? LocaleFactory.localizedString("Unknown") : failure.getDetail(),
                    LocaleFactory.localizedString("Cancel"), // default button
                    null, //other button
                    LocaleFactory.localizedString("Continue", "Credentials") // alternate button
            );
            alert.setShowsSuppressionButton(true);
            alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
            final AlertController sheet = new AlertController(controller, alert) {
                @Override
                protected void beginSheet(final NSWindow window) {
                    if(suppressed) {
                        c.set(option);
                    }
                    else {
                        super.beginSheet(window);
                    }
                }

                @Override
                public void callback(final int returncode) {
                    if(returncode == SheetCallback.DEFAULT_OPTION) {
                        c.set(false);
                    }
                    if(alert.suppressionButton().state() == NSCell.NSOnState) {
                        suppressed = true;
                        option = c.get();
                    }
                }
            };
            sheet.beginSheet();
            return c.get();
        }
        // Abort
        return false;
    }
}
