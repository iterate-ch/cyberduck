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

import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferErrorCallback;

public class PromptTransferErrorCallback implements TransferErrorCallback {

    private final WindowController controller;

    private boolean suppressed;

    private boolean option;

    public PromptTransferErrorCallback(final WindowController controller) {
        this.controller = controller;
    }

    @Override
    public boolean prompt(final BackgroundException failure) {
        if(suppressed) {
            return !option;
        }
        final NSAlert alert = NSAlert.alert();
        alert.setMessageText(null == failure.getMessage() ? LocaleFactory.localizedString("Unknown") : failure.getMessage());
        alert.setInformativeText(null == failure.getDetail() ? LocaleFactory.localizedString("Unknown") : failure.getDetail());
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Continue", "Credentials"));
        alert.setShowsSuppressionButton(true);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Always"));
        option = controller.alert(alert) == SheetCallback.DEFAULT_OPTION;
        if(alert.suppressionButton().state() == NSCell.NSOnState) {
            suppressed = true;
        }
        return !option;
    }
}
