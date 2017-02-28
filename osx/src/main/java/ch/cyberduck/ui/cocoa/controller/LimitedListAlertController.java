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
import ch.cyberduck.core.exception.ListCanceledException;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

public class LimitedListAlertController extends AlertController {
    private final ListCanceledException e;

    public LimitedListAlertController(final ListCanceledException e) {
        this.e = e;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(MessageFormat.format(LocaleFactory.localizedString("Listing directory {0}", "Status"), StringUtils.EMPTY));
        alert.setInformativeText(MessageFormat.format(LocaleFactory.localizedString("Continue listing directory with more than {0} files.", "Alert"), e.getChunk().size()));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Continue", "Credentials"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel"));
        alert.setShowsSuppressionButton(true);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
        super.loadBundle(alert);
    }
}
