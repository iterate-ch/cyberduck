package ch.cyberduck.ui.cocoa.callback;

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
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.ui.cocoa.controller.TransferErrorAlertController;

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
        final AlertController alert = new TransferErrorAlertController(failure);
        option = alert.beginSheet(controller) == SheetCallback.DEFAULT_OPTION;
        if(alert.isSuppressed()) {
            suppressed = true;
        }
        return !option;
    }
}
