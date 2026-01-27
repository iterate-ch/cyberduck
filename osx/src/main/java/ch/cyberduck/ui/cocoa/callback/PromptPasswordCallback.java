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

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.ui.cocoa.controller.PasswordController;

public class PromptPasswordCallback implements PasswordCallback {

    private final ProxyController controller;

    private PasswordController alert;
    private boolean suppressed;

    public PromptPasswordCallback(final ProxyController controller) {
        this.controller = controller;
    }

    @Override
    public void close(final String input) {
        if(null == alert) {
            return;
        }
        alert.setPasswordFieldText(input);
        alert.closeSheetWithOption(SheetCallback.ALTERNATE_OPTION);
    }

    @Override
    public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        if(suppressed) {
            throw new LoginCanceledException();
        }
        final Credentials credentials = new Credentials().setSaved(options.save);
        alert = new PasswordController(bookmark, credentials, title, reason, options);
        final int option = controller.alert(alert);
        if(option == SheetCallback.CANCEL_OPTION) {
            if(alert.isSuppressed()) {
                suppressed = true;
            }
            throw new LoginCanceledException();
        }
        return credentials;
    }
}
