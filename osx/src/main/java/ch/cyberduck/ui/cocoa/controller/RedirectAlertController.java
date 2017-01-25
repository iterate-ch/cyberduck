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

public class RedirectAlertController extends AlertController {
    private final String method;

    public RedirectAlertController(final String method) {
        this.method = method;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setMessageText(LocaleFactory.localizedString("Redirect"));
        alert.setInformativeText(LocaleFactory.localizedString(String.format("Allow redirect for method %s", method), "Alert"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Allow"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Alert"));
        alert.setShowsSuppressionButton(true);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
        super.loadBundle(alert);
    }
}
