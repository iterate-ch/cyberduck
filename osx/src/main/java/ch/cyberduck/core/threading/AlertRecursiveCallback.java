package ch.cyberduck.core.threading;

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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.worker.Worker;
import ch.cyberduck.ui.cocoa.AlertController;
import ch.cyberduck.ui.cocoa.WindowController;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;

public class AlertRecursiveCallback<T> implements Worker.RecursiveCallback<T> {

    private final WindowController controller;

    private boolean suppressed;

    private boolean option;

    public AlertRecursiveCallback(final WindowController controller) {
        this.controller = controller;
    }

    @Override
    public boolean recurse(final Path directory, final T value) {
        if(suppressed) {
            return option;
        }
        if(controller.isVisible()) {
            final AtomicBoolean c = new AtomicBoolean(false);
            final NSAlert alert = NSAlert.alert(
                    LocaleFactory.localizedString("Apply changes recursively"),
                    MessageFormat.format(LocaleFactory.localizedString("Do you want to set {0} on {1} recursively for all contained files?"),
                            value, directory.getName()),
                    LocaleFactory.localizedString("Continue", "Credentials"), // default button
                    null, //other button
                    LocaleFactory.localizedString("Cancel") // alternate button
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
                        c.set(true);
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
