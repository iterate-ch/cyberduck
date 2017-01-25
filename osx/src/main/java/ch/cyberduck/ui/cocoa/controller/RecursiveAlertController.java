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
import ch.cyberduck.core.Path;

import java.text.MessageFormat;

public class RecursiveAlertController<T> extends AlertController {
    private final T value;
    private final Path directory;

    public RecursiveAlertController(final T value, final Path directory) {
        this.value = value;
        this.directory = directory;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Apply changes recursively"));
        alert.setInformativeText(MessageFormat.format(LocaleFactory.localizedString("Do you want to set {0} on {1} recursively for all contained files?"),
                value, directory.getName()));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Continue", "Credentials"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel"));
        alert.setShowsSuppressionButton(true);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
        super.loadBundle(alert);
    }
}
