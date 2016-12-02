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

import ch.cyberduck.binding.DisabledSheetCallback;
import ch.cyberduck.binding.SheetInvoker;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.ui.cocoa.controller.PasswordController;

public class PromptPasswordCallback implements PasswordCallback {

    private final WindowController parent;

    public PromptPasswordCallback(final WindowController parent) {
        this.parent = parent;
    }

    @Override
    public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        final PasswordController controller = new PasswordController(parent, credentials, title, reason, options);
        final SheetInvoker sheet = new SheetInvoker(new DisabledSheetCallback(), parent, controller);
        final int option = sheet.beginSheet();
        if(option == SheetCallback.CANCEL_OPTION) {
            throw new LoginCanceledException();
        }
    }
}
